package io.redlink.more.studymanager.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.DateHistogramBucket;
import co.elastic.clients.elasticsearch._types.aggregations.MinimumInterval;
import co.elastic.clients.elasticsearch._types.aggregations.MultiBucketBase;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.util.ObjectBuilder;
import io.redlink.more.studymanager.core.io.TimeRange;
import io.redlink.more.studymanager.core.ui.DataViewData;
import io.redlink.more.studymanager.core.ui.DataViewRow;
import io.redlink.more.studymanager.core.ui.ViewConfig;
import io.redlink.more.studymanager.model.StudyGroup;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static io.redlink.more.studymanager.service.ElasticService.getFilters;
import static io.redlink.more.studymanager.service.ElasticService.getStudyIdString;

@Service
public class ElasticDataService {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticDataService.class);

    private static final String AGG_NAME_SERIES = "series";
    private static final String AGG_NAME_ROWS = "rows";
    private static final String AGG_NAME_VALUES = "values";
    private static final String NO_GROUP_KEY = "no_group";

    private final ElasticsearchClient client;

    private final ParticipantService participantService;
    private final StudyGroupService studyGroupService;

    public ElasticDataService(ElasticsearchClient client, ParticipantService participantService, StudyGroupService studyGroupService) {
        this.client = client;
        this.participantService = participantService;
        this.studyGroupService = studyGroupService;
    }

    public DataViewData queryObservationViewData(ViewConfig viewConfig, long studyId, Integer studyGroupId, int observationId, Integer participantId, TimeRange timerange) throws IOException {
        final List<Query> filters = getFilters(studyId, observationId, studyGroupId, participantId, timerange);

        final SearchRequest.Builder builder = buildDataPreviewRequest(viewConfig, filters, studyId);
        final SearchRequest request = builder.build();

        try {
            final SearchResponse<Void> searchResponse = client.search(request, Void.class);
            return processDataPreviewResponse(viewConfig, searchResponse, studyId);
        } catch (IOException | ElasticsearchException e) {
            return ElasticService.handleIndexNotFoundException(e, () -> null, IOException::new);
        }
    }

    private SearchRequest.Builder buildDataPreviewRequest(ViewConfig viewConfig, List<Query> filters, long studyId) {
        final var rows = viewConfig.rowAggregation();
        final var series = viewConfig.seriesAggregation();
        return new SearchRequest.Builder()
                .index(getStudyIdString(studyId))
                .size(0)
                .query(q -> q.bool(b -> b.filter(filters)))
                .aggregations(AGG_NAME_SERIES, s ->
                        applyAggregation(s, series, viewConfig.operation())
                                .aggregations(AGG_NAME_ROWS, r ->
                                        applyAggregation(r, rows, viewConfig.operation())
                                                .aggregations(AGG_NAME_VALUES, d -> applyOperation(d, viewConfig))
                                )

                )
                .aggregations("rowLabels", rl ->
                        applyAggregation(rl, rows, viewConfig.operation())
                )
                ;
    }

    private List<? extends MultiBucketBase> readBuckets(Map<String, Aggregate> aggregations, String aggName) {
        final var agg = aggregations.get(aggName);
        if (agg.isAutoDateHistogram()) {
            return agg.autoDateHistogram()
                    .buckets().array();
        } else if (agg.isSterms()) {
            return agg.sterms()
                    .buckets().array();
        }
        throw new IllegalStateException("Unknown aggregation type: " + agg._kind());
    }

    private String readBucketKey(MultiBucketBase bucket) {
        if (bucket instanceof StringTermsBucket str) {
            return str.key().stringValue();
        } else if (bucket instanceof DateHistogramBucket date) {
            return date.keyAsString();
        } else {
            return null;
        }
    }

    private Function<String, String> createTitleResolver(ViewConfig.Aggregation aggregation, long studyId) {
        if (aggregation == null) {
            return Function.identity();
        }

        final Map<String, String> mapping =  switch (aggregation) {
            case PARTICIPANT ->
                participantService.listParticipants(studyId)
                        .stream()
                        .collect(Collectors.toMap(
                                p -> ElasticService.getParticipantIdString(p.getParticipantId()),
                                p -> String.format("%s (%d)", p.getAlias(), p.getParticipantId())
                        ));

            case STUDY_GROUP -> {
                final Map<String, String> m = new HashMap<>(
                        studyGroupService.listStudyGroups(studyId)
                        .stream()
                        .collect(Collectors.toMap(
                                g -> ElasticService.getStudyGroupIdString(g.getStudyGroupId()),
                                StudyGroup::getTitle
                        ))
                );
                m.put(NO_GROUP_KEY, "i18n.global.placeholder.noGroup");
                yield m;
            }
            default -> Map.of();
        };
        return l -> mapping.getOrDefault(l, l);
    }

    private DataViewData processDataPreviewResponse(ViewConfig viewConfig, SearchResponse<Void> searchResponse, long studyId) {

        final List<? extends MultiBucketBase> seriesBuckets = readBuckets(searchResponse.aggregations(), AGG_NAME_SERIES);
        final int seriesCount = seriesBuckets.size();
        final List<String> labels = new ArrayList<>(seriesCount);
        final Supplier<ArrayList<Double>> createValueArray = () -> {
            final ArrayList<Double> array = new ArrayList<>(seriesCount);
            for (int i = 0; i < seriesCount; i++) {
                array.add(null);
            }
            return array;
        };

        final LinkedHashMap<String, List<Double>> rowMap = new LinkedHashMap<>();

        final Function<String, String> labelsTitleResolver = createTitleResolver(viewConfig.seriesAggregation(), studyId);
        for (MultiBucketBase bucket : seriesBuckets) {
            final String bucketKey = readBucketKey(bucket);

            labels.add(labelsTitleResolver.apply(bucketKey));
            final int seriesIdx = labels.size() - 1;

            final List<? extends MultiBucketBase> rowsBuckets = readBuckets(bucket.aggregations(), AGG_NAME_ROWS);
            for (MultiBucketBase rowBucket : rowsBuckets) {
                final String rowKey = readBucketKey(rowBucket);

                final var valueAgg = rowBucket.aggregations().get(AGG_NAME_VALUES);
                final var value = switch (viewConfig.operation().operator()) {
                    case SUM, COUNT -> valueAgg.sum().value();
                    case MIN -> valueAgg.min().value();
                    case MAX -> valueAgg.max().value();
                    case AVG -> valueAgg.avg().value();
                };
                rowMap.computeIfAbsent(rowKey, k -> createValueArray.get())
                        .set(seriesIdx, value);
            }
        }

        final Function<String, String> rowTitleResolver = createTitleResolver(viewConfig.rowAggregation(), studyId);
        return new DataViewData(
                List.copyOf(labels),
                rowMap.entrySet().stream()
                        .map(e -> new DataViewRow(
                                rowTitleResolver.apply(e.getKey()),
                                e.getValue()
                        ))
                        .toList()
        );
    }

    private Aggregation.Builder.ContainerBuilder applyAggregation(Aggregation.Builder a, ViewConfig.Aggregation aggregation, ViewConfig.Operation operation) {
        if (aggregation == null) {
            // If there's no aggregation required at this level, we perform
            //   "no-op"-aggregation to keep response-structure aligned.
            return a.terms(n -> n.field("study_id.keyword"));
        }
        return switch (aggregation) {
            case TIME -> a.autoDateHistogram(dateHistogram -> dateHistogram
                    .field("effective_time_frame")
                    .buckets(1000) // TODO: Hidden magic number, aligned with chart-width in the UI!
                    .minimumInterval(MinimumInterval.Minute)
                    .format("yyyy-MM-dd'T'HH:mmZ")
            );
            case PARTICIPANT -> a.terms(pt -> pt.field("participant_id.keyword"));
            case STUDY_GROUP -> a.terms(sg -> sg.field("study_group_id.keyword")
                    .minDocCount(0)
                    .missing(NO_GROUP_KEY)
            );
            case TERM_FIELD -> a.terms(tf -> tf.field("data_%s.keyword".formatted(operation.field()))
                    .minDocCount(0)
            );
        };
    }

    private ObjectBuilder<Aggregation> applyOperation(Aggregation.Builder agg, ViewConfig viewConfig) {
        final String field = viewConfig.operation().field();
        return switch (viewConfig.operation().operator()) {
            case AVG -> agg.avg(
                    s -> s.field(String.format("data_%s", field))
            );
            case SUM -> agg.sum(
                    s -> s.field(String.format("data_%s", field))
            );
            case MIN -> agg.min(
                    m -> m.field(String.format("data_%s", field))
            );
            case MAX -> agg.max(
                    m -> m.field(String.format("data_%s", field))
            );
            case COUNT -> agg.sum(
                    s -> s.field("non_existing_field")
                            .missing(1)
            );
        };
    }


}
