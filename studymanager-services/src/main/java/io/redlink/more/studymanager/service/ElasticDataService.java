/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.util.ObjectBuilder;
import io.redlink.more.studymanager.core.datavalidity.*;
import io.redlink.more.studymanager.core.io.TimeRange;
import io.redlink.more.studymanager.core.measurement.Measurement;
import io.redlink.more.studymanager.core.measurement.MeasurementSet;
import io.redlink.more.studymanager.core.ui.DataViewData;
import io.redlink.more.studymanager.core.ui.DataViewRow;
import io.redlink.more.studymanager.core.ui.ViewConfig;
import io.redlink.more.studymanager.model.StudyGroup;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
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

    public DataViewData queryObservationViewData(ViewConfig viewConfig, Long studyId, Integer studyGroupId, Integer observationId, Integer participantId, TimeRange timerange) throws IOException {
        return queryObservationViewData(viewConfig, studyId, studyGroupId, observationId, participantId, null, timerange);
    }

    public DataViewData queryObservationViewData(ViewConfig viewConfig, Long studyId, Integer studyGroupId, Integer observationId, Integer participantId, String dataType, TimeRange timerange) throws IOException {
        final List<Query> filters = getFilters(studyId, observationId, studyGroupId, participantId, dataType, timerange);

        final SearchRequest.Builder builder = buildDataPreviewRequest(viewConfig, filters, studyId);
        final SearchRequest request = builder.build();

        try {
            final SearchResponse<Void> searchResponse = client.search(request, Void.class);
            return processDataPreviewResponse(viewConfig, searchResponse, studyId);
        } catch (IOException | ElasticsearchException e) {
            return ElasticService.handleIndexNotFoundException(e, () -> null, IOException::new);
        }
    }

    public ObservationDataSummary validateObservationData(
            long studyId, Integer studyGroupId, int observationId, int participantId,
            TimeRange timerange, MeasurementSet measurementSet
    ) throws IOException {
        final List<Query> filters = getFilters(studyId, observationId, studyGroupId, participantId, null, timerange);
        final SearchRequest.Builder requestBuilder = new SearchRequest.Builder()
                .index(getStudyIdString(studyId))
                .query(q -> q.bool(b -> b.filter(filters)))
                .size(0);
        if (measurementSet != null) {
            buildMeasurementSetAggregations(requestBuilder, measurementSet);
        } //else no data fields to validate ... just check if any documents are present
        final SearchRequest request = requestBuilder.build();

        try {
            final SearchResponse<Void> searchResponse = client.search(request, Void.class);
            return parseValidationResults(measurementSet, searchResponse, studyId);
        } catch (IOException | ElasticsearchException e) {
            return ElasticService.handleIndexNotFoundException(e, () -> null, IOException::new);
        }
    }

    private SearchRequest.Builder buildMeasurementSetAggregations(SearchRequest.Builder builder, MeasurementSet measurementSet) {
        for (Measurement measurement : measurementSet.values()) {
            String id = measurement.getId();
            if (id == null) {
                continue;
            }
            String field = "data_" + id;
            Measurement.Type type = measurement.getType();
            switch (type) {
                case STRING:
                    String termsName = field + "_counts";
                    builder.aggregations(termsName, Aggregation.of(a -> a.terms(t -> t.field(field + ".keyword").missing("missing"))));
                    break;
                case BOOLEAN:
                    String boolTermsName = field + "_counts";
                    builder.aggregations(boolTermsName, Aggregation.of(a -> a.terms(t -> t.field(field).missing("missing"))));
                    break;
                case INTEGER:
                case DOUBLE:
                case DATE:
                    String statsName = field + "_stats";
                    builder.aggregations(statsName, Aggregation.of(a -> a.stats(s -> s.field(field))));
                    String missingName = field + "_missing";
                    builder.aggregations(missingName, Aggregation.of(a -> a.missing(m -> m.field(field))));
                    break;
                case OBJECT:
                    // do nothing
                    break;
            }
        }
        // Add aggregations for effective_time_frame
        builder.aggregations("min_effective_time_frame", Aggregation.of(a -> a.min(m -> m.field("effective_time_frame"))));
        builder.aggregations("max_effective_time_frame", Aggregation.of(a -> a.max(m -> m.field("effective_time_frame"))));
        builder.aggregations("effective_time_frame_missing", Aggregation.of(a -> a.missing(m -> m.field("effective_time_frame"))));
        // Add aggregation for numDocs
        builder.aggregations("total_docs", Aggregation.of(a -> a.valueCount(v -> v.field("study_id.keyword"))));
        return builder;
    }

    private ObservationDataSummary parseValidationResults(MeasurementSet measurementSet, SearchResponse<Void> searchResponse, long studyId) {
        long numDocs;
        DateMeasurementSummary effectiveTime = null;
        List<MeasurementSummary> measurements = Collections.emptyList();

        if (searchResponse.aggregations().isEmpty()) {
            // measurementSet was null
            numDocs = searchResponse.hits().total().value();
        } else {
            ValueCountAggregate totalAgg = searchResponse.aggregations().get("total_docs").valueCount();
            numDocs = (long) totalAgg.value();

            // Parse effective_time_frame
            MinAggregate minEffAgg = searchResponse.aggregations().get("min_effective_time_frame").min();
            double minVal = minEffAgg.value();
            Instant minInst = (minVal == Double.POSITIVE_INFINITY) ? null : Instant.ofEpochMilli((long) minVal);

            MaxAggregate maxEffAgg = searchResponse.aggregations().get("max_effective_time_frame").max();
            double maxVal = maxEffAgg.value();
            Instant maxInst = (maxVal == Double.NEGATIVE_INFINITY) ? null : Instant.ofEpochMilli((long) maxVal);

            MissingAggregate missEffAgg = searchResponse.aggregations().get("effective_time_frame_missing").missing();
            long effMissing = missEffAgg.docCount();

            effectiveTime = new DateMeasurementSummary(minInst, maxInst, effMissing);

            // Parse measurements
            measurements = new ArrayList<>();
            for (Measurement measurement : measurementSet.values()) {
                String id = measurement.getId();
                if (id == null) {
                    continue;
                }
                String field = "data_" + id;
                Measurement.Type type = measurement.getType();
                MeasurementSummary ms = new MeasurementSummary(measurement);

                switch (type) {
                    case STRING:
                        String termsName = field + "_counts";
                        StringTermsAggregate stringTerms = searchResponse.aggregations().get(termsName).sterms();
                        List<StringFieldValue> stringValues = new ArrayList<>();
                        for (StringTermsBucket bucket : stringTerms.buckets().array()) {
                            String key = bucket.key().stringValue();
                            long count = bucket.docCount();
                            String value = "missing".equals(key) ? null : key;
                            stringValues.add(new StringFieldValue(value, count));
                        }
                        ms.setStringResult(new StringMeasurementSummary(stringValues));
                        break;
                    case BOOLEAN:
                        String boolTermsName = field + "_counts";
                        StringTermsAggregate boolTerms = searchResponse.aggregations().get(boolTermsName).sterms();
                        List<BooleanFieldValue> boolValues = new ArrayList<>();
                        for (StringTermsBucket bucket : boolTerms.buckets().array()) {
                            String key = bucket.key().stringValue();
                            long count = bucket.docCount();
                            Boolean value;
                            if ("missing".equals(key)) {
                                value = null;
                            } else if ("true".equals(key)) {
                                value = Boolean.TRUE;
                            } else if ("false".equals(key)) {
                                value = Boolean.FALSE;
                            } else {
                                continue; // unexpected
                            }
                            boolValues.add(new BooleanFieldValue(value, count));
                        }
                        ms.setBooleanResult(new BooleanMeasurementSummary(boolValues));
                        break;
                    case INTEGER:
                    case DOUBLE:
                        String numericStatsName = field + "_stats";
                        StatsAggregate numericStats = searchResponse.aggregations().get(numericStatsName).stats();
                        String numericMissingName = field + "_missing";
                        MissingAggregate numericMiss = searchResponse.aggregations().get(numericMissingName).missing();
                        double min = (numericStats.min() == Double.POSITIVE_INFINITY) ? Double.NaN : numericStats.min();
                        double max = (numericStats.max() == Double.NEGATIVE_INFINITY) ? Double.NaN : numericStats.max();
                        double avg = numericStats.avg();
                        double sum = numericStats.sum();
                        long missing = numericMiss.docCount();
                        ms.setNumericResult(new NumericMeasurementSummary(min, max, avg, sum, missing));
                        break;
                    case DATE:
                        String dateStatsName = field + "_stats";
                        StatsAggregate dateStats = searchResponse.aggregations().get(dateStatsName).stats();
                        String dateMissingName = field + "_missing";
                        MissingAggregate dateMiss = searchResponse.aggregations().get(dateMissingName).missing();
                        double dateMinVal = dateStats.min();
                        Instant dateMinInst = (dateMinVal == Double.POSITIVE_INFINITY) ? null : Instant.ofEpochMilli((long) dateMinVal);
                        double dateMaxVal = dateStats.max();
                        Instant dateMaxInst = (dateMaxVal == Double.NEGATIVE_INFINITY) ? null : Instant.ofEpochMilli((long) dateMaxVal);
                        long dateMissing = dateMiss.docCount();
                        ms.setDateResult(new DateMeasurementSummary(dateMinInst, dateMaxInst, dateMissing));
                        break;
                    case OBJECT:
                        // do nothing
                        break;
                }
                measurements.add(ms);
            }
        }

        return new ObservationDataSummary(numDocs, effectiveTime, measurements);
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

        final Map<String, String> mapping = switch (aggregation) {
            case PARTICIPANT -> participantService.listParticipants(studyId)
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
                    .minDocCount(1)
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
