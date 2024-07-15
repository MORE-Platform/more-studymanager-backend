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
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static io.redlink.more.studymanager.service.ElasticService.getFilters;
import static io.redlink.more.studymanager.service.ElasticService.getStudyIdString;

@Service
public class ElasticDataService {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticDataService.class);

    private final ElasticsearchClient client;

    public ElasticDataService(ElasticsearchClient client) {
        this.client = client;
    }

    public DataViewData queryObservationViewData(ViewConfig viewConfig, long studyId, Integer studyGroupId, int observationId, Integer participantId, TimeRange timerange) {
        final List<Query> filters = getFilters(studyId, observationId, studyGroupId, participantId, timerange);

        final SearchRequest.Builder builder = buildDataPreviewRequest(viewConfig, filters, studyId);
        final SearchRequest request = builder.build();

        try {
            SearchResponse<Void> searchResponse = client.search(request, Void.class);
            return processDataPreviewResponse(viewConfig, searchResponse);
        } catch (IOException | ElasticsearchException e) {
            if (e instanceof ElasticsearchException ee) {
                if (Objects.equals(ee.error().type(), "index_not_found_exception")) {
                    return null;
                }
            }
            LOG.warn("Elastic Query failed", e);
            return null;
        }
    }

    private SearchRequest.Builder buildDataPreviewRequest(ViewConfig viewConfig, List<Query> filters, long studyId) {
        final var rows = viewConfig.rowAggregation();
        final var series = viewConfig.seriesAggregation();
        return new SearchRequest.Builder()
                .index(getStudyIdString(studyId))
                .size(0)
                .query(q -> q.bool(b -> b.filter(filters)))
                .aggregations("series", s ->
                        applyAggregation(s, series, viewConfig.operation())
                                .aggregations("rows", r ->
                                        applyAggregation(r, rows, viewConfig.operation())
                                                .aggregations("values", d -> applyOperation(d, viewConfig))
                                )

                )
                .aggregations("rowLabels", rl ->
                        applyAggregation(rl, rows, viewConfig.operation())
                )
                ;
    }

    private DataViewData processDataPreviewResponse(ViewConfig viewConfig, SearchResponse<Void> searchResponse) {
        List<? extends MultiBucketBase> seriesBuckets;
        if (viewConfig.seriesAggregation() == ViewConfig.Aggregation.TIME) {
            seriesBuckets = searchResponse.aggregations().get("series")
                    .autoDateHistogram()
                    .buckets().array();
        } else {
            seriesBuckets = searchResponse.aggregations().get("series")
                    .sterms()
                    .buckets().array();
        }

        final LinkedList<String> labels = new LinkedList<>();
        final LinkedHashMap<String, List<Double>> rowMap = new LinkedHashMap<>();
        final int seriesCount = seriesBuckets.size();
        final Supplier<ArrayList<Double>> genArray = () -> {
            ArrayList<Double> array = new ArrayList<>(seriesCount);
            for (int i = 0; i < seriesCount; i++) {
                array.add(null);
            }
            return array;
        };

        for (MultiBucketBase bucket : seriesBuckets) {
            final String bucketKey;
            if (bucket instanceof StringTermsBucket str) {
                bucketKey = str.key().stringValue();
            } else if (bucket instanceof DateHistogramBucket date) {
                bucketKey = date.keyAsString();
            } else {
                continue;
            }

            labels.add(bucketKey);
            final int seriesIdx = labels.indexOf(bucketKey);

            final Aggregate rows = bucket.aggregations().get("rows");
            final List<? extends MultiBucketBase> rowsBuckets;
            if (viewConfig.rowAggregation() == ViewConfig.Aggregation.TIME) {
                rowsBuckets = rows.autoDateHistogram().buckets().array();
            } else {
                rowsBuckets = rows.sterms().buckets().array();
            }

            for (MultiBucketBase rowBucket : rowsBuckets) {
                final String rowKey;
                if (rowBucket instanceof StringTermsBucket str) {
                    rowKey = str.key().stringValue();
                } else if (rowBucket instanceof DateHistogramBucket date) {
                    rowKey = date.keyAsString();
                } else {
                    continue;
                }

                final var valueAgg = rowBucket.aggregations().get("values");
                final var value = switch (viewConfig.operation().operator()) {
                    case SUM, COUNT -> valueAgg.sum().value();
                    case MIN -> valueAgg.min().value();
                    case MAX -> valueAgg.max().value();
                    case AVG -> valueAgg.avg().value();
                };
                rowMap.computeIfAbsent(rowKey, k -> genArray.get()).set(seriesIdx, value);
            }
        }

        return new DataViewData(
                List.copyOf(labels),
                rowMap.entrySet().stream()
                        .map(e -> new DataViewRow(
                                e.getKey(),
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
                    .buckets(500) // TODO: Hidden magic number!
                    .minimumInterval(MinimumInterval.Minute)
                    .format("yyyy-MM-dd'T'HH:mmZ")
            );
            case PARTICIPANT -> a.terms(pt -> pt.field("participant_id.keyword")
                    .minDocCount(0)
            );
            case STUDY_GROUP -> a.terms(sg -> sg.field("study_group_id.keyword")
                    .minDocCount(0)
                    .missing("no_group")
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
