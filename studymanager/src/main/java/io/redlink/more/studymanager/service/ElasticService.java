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
import co.elastic.clients.elasticsearch._types.Conflicts;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import co.elastic.clients.elasticsearch.core.DeleteByQueryRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CloseIndexRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.json.JsonData;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Iterables;
import io.redlink.more.studymanager.core.io.TimeRange;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.data.ElasticDataPoint;
import io.redlink.more.studymanager.model.data.ParticipationData;
import io.redlink.more.studymanager.model.data.SimpleDataPoint;
import io.redlink.more.studymanager.properties.ElasticProperties;
import io.redlink.more.studymanager.utils.MapperUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@EnableConfigurationProperties({ElasticProperties.class})
public class ElasticService {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticService.class);

    private static final int BATCH_SIZE_FOR_EXPORT_REQUESTS = 1000;

    private final ElasticsearchClient client;

    public ElasticService(ElasticsearchClient client) { this.client = client; }

    public List<Integer> participantsThatMapQuery(Long studyId, Integer studyGroupId, String query, TimeRange timerange) {
        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder
                .index(getStudyIdString(studyId))
                .size(0)
                .query(q -> q.
                        bool(b -> b.
                                must(m -> m.
                                        queryString(qs -> qs.
                                                query(query))).
                                filter(getFilters(studyId, studyGroupId, timerange))
                        )).aggregations(
                        "participant_ids",
                        a -> a.terms(t -> t.
                                field("participant_id.keyword").
                                size(10000))
                );

        try {
            if(indexExists(studyId)) {
                return client.search(builder.build(), Map.class)
                        .aggregations().get("participant_ids").sterms().buckets().array().stream()
                        .map(StringTermsBucket::key)
                        .map(FieldValue::stringValue)
                        .map(s -> s.substring(12))
                        .map(Integer::valueOf)
                        .toList();
            } else {
                return List.of();
            }
        } catch (IOException | ElasticsearchException e) {
            LOG.error("Elastic Query failed", e);
            return List.of();
        }
    }

    static List<Query> getFilters(Long studyId, Integer studyGroupId, TimeRange timerange) {
        List<Query> queries = new ArrayList<>();
        queries.add(getStudyFilter(studyId));
        if (studyGroupId != null) {
            queries.add(getStudyGroupFilter(studyGroupId));
        }

        if (timerange != null && timerange.getFromString() != null && timerange.getToString() != null) {
            queries.add(getTimeRangeFilter(timerange));
        }
        return queries;
    }

    static List<Query> getFilters(Long studyId, Integer observationId, Integer studyGroupId, Integer participantId, TimeRange timerange) {
        List<Query> filters = new ArrayList<>();
        filters.add(getStudyFilter(studyId));
        filters.add(getObservationFilter(observationId));

        if (participantId != null) {
            filters.add(getParticipantFilter(participantId));
        } else if(studyGroupId != null) {
            filters.add(getStudyGroupFilter(studyGroupId));
        }

        if (timerange != null && timerange.getFromString() != null && timerange.getToString() != null) {
            filters.add(getTimeRangeFilter(timerange));
        }
        return filters;
    }

    static Query getStudyFilter(Long studyId) {
        return Query.of(f -> f.
                term(t -> t.
                        field("study_id.keyword").
                        value(getStudyIdString(studyId))));
    }

    static Query getObservationFilter(Integer observationId) {
        return Query.of(f -> f.
                term(t -> t.
                        field("observation_id.keyword").
                        value(observationId)));
    }

    static Query getStudyGroupFilter(Integer studyGroupId) {
        return Query.of(f -> f.
                term(t -> t.
                        field("study_group_id.keyword").
                        value(getStudyGroupIdString(studyGroupId))));
    }

    static Query getParticipantFilter(Integer participantId) {
        return Query.of(f -> f.
                term(t -> t.
                        field("participant_id.keyword").
                        value(getParticipantIdString(participantId))));
    }

    static Query getTimeRangeFilter(TimeRange timerange) {
        return Query.of(f -> f.
                range(r -> r.
                        field("effective_time_frame").
                        from(timerange.getFromString()).
                        to(timerange.getToString())
                ));
    }

    public boolean indexExists(long studyId) {
        ExistsRequest request = new ExistsRequest.Builder()
                .index(getStudyIdString(studyId))
                .build();
        try {
            return this.client.indices().exists(request).value();
        } catch (IOException e) {
            return false;
        }
    }

    public boolean closeIndex(Study study) {
        try {
            CloseIndexRequest indexRequest = new CloseIndexRequest.Builder()
                    .index(getStudyIdString(study))
                    .build();
            this.client.indices().close(indexRequest);
            return true;
        } catch (IOException | ElasticsearchException e) {
            LOG.warn("Error when closing elastic index. Error message: ", e);
            return false;
        }
    }

    public boolean deleteIndex(Study study) {
        return deleteIndex(study.getStudyId());
    }

    public boolean deleteIndex(Long studyId) {
        try {
            DeleteIndexRequest indexRequest = new DeleteIndexRequest.Builder()
                    .index(getStudyIdString(studyId))
                    .ignoreUnavailable(true)
                    .build();
            this.client.indices().delete(indexRequest);
            return true;
        } catch (IOException | ElasticsearchException e) {
            LOG.warn("Error when deleting elastic index. Error message: ", e);
            return false;
        }
    }

    public void removeDataForParticipant(Long studyId, Integer participantId) {
        if (!indexExists(studyId)) { return; }
        try {
            DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest.Builder()
                    .index(getStudyIdString(studyId))
                    .query(q -> q
                            .bool(b -> b
                                    .filter(f -> f.term(t -> t
                                            .field("participant_id.keyword")
                                            .value("participant_" + participantId)))))
                    .conflicts(Conflicts.Proceed)
                    .build();
            client.deleteByQuery(deleteByQueryRequest);
        } catch (IOException | ElasticsearchException e) {
            handleIndexNotFoundException(e, t -> {
                LOG.warn("Error when deleting participant from elastic index. Error message: ", e);
                return new RuntimeException(t);
            });
        }
    }

    static String getStudyIdString(Study study) {
        return getStudyIdString(study.getStudyId());
    }

    static String getStudyGroupIdString(Integer studyGroupId) {
        return "study_group_" + studyGroupId;
    }

    static String getStudyIdString(Long id) {
        return "study_" + id;
    }

    static String getParticipantIdString(Integer participantId) {
        return "participant_" + participantId;
    }

    public void setDataPoint(Long studyId, ElasticDataPoint elasticActionDataPoint) {
        try {
            client.index(i -> i.index(getStudyIdString(studyId)).document(elasticActionDataPoint));
        } catch (IOException e) {
            LOG.warn("Could nor store datapoint", e);
        }
    }

    public List<ParticipationData> getParticipationData(Long studyId){
        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder.index(getStudyIdString(studyId))
                .size(0)
                .aggregations("observation_ids",
                        a -> a.terms(t -> t
                                        .field("observation_id.keyword")
                                        .size(10000))
                                .aggregations("participant_ids",
                                        a3 -> a3.terms(t -> t
                                                        .field("participant_id.keyword")
                                                        .size(10000))
                                                .aggregations("latest_data",
                                                        a4 -> a4.max(m -> m
                                                                .field("storage_date"))
                                                ).aggregations("study_group_id",
                                                        a5 -> a5.terms(t -> t
                                                                .field("study_group_id.keyword")
                                                                .size(10000))
                                                )
                                )
                );
        try{
            List<ParticipationData> participationDataList = new ArrayList<>();

            List<StringTermsBucket> observationBuckets =  client.search(builder.build(), Map.class)
                    .aggregations()
                    .get("observation_ids")
                    .sterms()
                    .buckets()
                    .array();
            for(StringTermsBucket observation : observationBuckets){
                List<StringTermsBucket> participantBuckets = observation
                        .aggregations()
                        .get("participant_ids")
                        .sterms()
                        .buckets()
                        .array();
                for(StringTermsBucket participant : participantBuckets){
                    String lastDataReceived = participant
                            .aggregations()
                            .get("latest_data")
                            .max()
                            .valueAsString();
                    List<StringTermsBucket> studyGroupBuckets = participant
                            .aggregations()
                            .get("study_group_id")
                            .sterms()
                            .buckets()
                            .array();
                    ParticipationData.NamedId studyGroup = null;
                    if(studyGroupBuckets.size() > 0)
                             studyGroup = new ParticipationData.NamedId(Integer.parseInt(
                                     studyGroupBuckets
                                     .get(0)
                                     .key()
                                     .stringValue()
                                     .substring(12)), null);
                    assert lastDataReceived != null;
                    participationDataList.add(new ParticipationData(
                            new ParticipationData.NamedId(Integer.parseInt(observation.key().stringValue().replaceAll("observation_", "")), null),
                            "observationType",
                            new ParticipationData.NamedId(Integer.parseInt(participant.key().stringValue().substring(12)), null),
                            studyGroup,
                            true,
                            Instant.parse(lastDataReceived)));
                }
            }
            return participationDataList;
        }catch (IOException | ElasticsearchException e) {
            if (isElasticIndexNotFound(e)) {
                return List.of();
            }
            LOG.warn("Elastic Query failed", e);
            return new ArrayList<>();
        }
    }

    public void exportData(OutputStream outputStream, Long studyId, List<Integer> studyGroupId, List<Integer> participantId, List<Integer> observationId, Instant from, Instant to) throws IOException {
        String index = getStudyIdString(studyId);

        if(!client.indices().exists(e -> e.index(index)).value()) {
            return;
        }

        SearchRequest request = getQuery(index, studyGroupId, participantId, observationId, from, to, null);
        SearchResponse<JsonNode> rsp = client.search(request, JsonNode.class);

        while (rsp.hits().hits().size() > 0) {
            writeHits(rsp.hits().hits(), outputStream);
            outputStream.flush();
            List<FieldValue> searchAfterSort = Iterables.getLast(rsp.hits().hits()).sort();
            request = getQuery(index, studyGroupId, participantId, observationId, from, to, searchAfterSort);
            rsp = client.search(request, JsonNode.class);
            if (rsp.hits().hits().size() > 0) {
                outputStream.write(",".getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private void writeHits(List<Hit<JsonNode>> hits, OutputStream outputStream) throws IOException {
        String datapoints = hits.stream()
                .map(Hit::source)
                .map(MapperUtils::writeValueAsString)
                .collect(Collectors.joining(","));
        outputStream.write(datapoints.getBytes(StandardCharsets.UTF_8));
    }

    private SearchRequest getQuery(String index, List<Integer> studyGroupIds, List<Integer> participantIds, List<Integer> observationIds, Instant from, Instant to, List<FieldValue> searchAfterSort) {
        SearchRequest.Builder builder = new SearchRequest.Builder();
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        if (studyGroupIds != null && !studyGroupIds.isEmpty()) {
            List<String> studyGroupIdsStrings = studyGroupIds.stream()
                    .map(ElasticService::getStudyGroupIdString)
                    .toList();
            boolQueryBuilder.filter(f -> f.terms(t -> t.field("study_group_id").terms(TermsQueryField.of(tf -> tf.value(studyGroupIdsStrings.stream().map(FieldValue::of).collect(Collectors.toList()))))));
        }

        if (participantIds != null && !participantIds.isEmpty()) {
            List<String> participantIdStrings = participantIds.stream()
                    .map(ElasticService::getParticipantIdString)
                    .toList();
            boolQueryBuilder.filter(f -> f.terms(t -> t.field("participant_id").terms(TermsQueryField.of(tf -> tf.value(participantIdStrings.stream().map(FieldValue::of).collect(Collectors.toList()))))));
        }

        if (observationIds != null && !observationIds.isEmpty()) {
            boolQueryBuilder.filter(f -> f.terms(t -> t.field("observation_id").terms(v -> v.value(observationIds.stream().map(FieldValue::of).collect(Collectors.toList())))));
        }

        if (from != null && to != null) {
            boolQueryBuilder.filter(f -> f.range(r -> r.field("effective_time_frame").gte(JsonData.of(from)).lte(JsonData.of(to))));
        }

        builder.index(index)
                .query(q -> q.bool(boolQueryBuilder.build()))
                .sort(s -> s.field(f -> f.field("effective_time_frame").order(SortOrder.Asc)))
                .sort(s -> s.field(f -> f.field("datapoint_id.keyword").order(SortOrder.Asc)))
                .size(BATCH_SIZE_FOR_EXPORT_REQUESTS);

        if (searchAfterSort != null) {
            builder.searchAfter(searchAfterSort);
        }

        return builder.build();
    }


    public List<SimpleDataPoint> listDataPoints(
            Long studyId, Integer participantId, Integer observationId, String isoDate, int size) throws IOException {
        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder.query(q -> q.bool(b ->b.must(m ->
                        m.term(t -> t.field("study_id").value("study_" + studyId)))
                        .filter(getFilters(participantId, observationId, isoDate))))
                .sort(s -> s.field(f -> f.field("effective_time_frame").order(SortOrder.Desc)))
                .size(size);

        SearchResponse<Map> rsp = client.search(builder.build(), Map.class);

        return rsp.hits().hits().stream().map(h -> new SimpleDataPoint()
                .setObservationId(Integer.parseInt(h.source().get("observation_id").toString()))
                .setParticipantId(Integer.parseInt(h.source().get("participant_id").toString().substring(12)))
                .setTime(h.source().get("effective_time_frame").toString())
                .setData(toData(h.source()))).toList();
    }

    private List<Query> getFilters(Integer participantId, Integer observationId, String isoDate) {
        List<Query> filters = new ArrayList<>();
        if(participantId != null) {
            filters.add(Query.of(q -> q.term(t -> t.field("participant_id").value("participant_" + participantId))));
        }

        if(observationId != null) {
            filters.add(Query.of(q -> q.term(t -> t.field("observation_id").value(observationId))));
        } else {
            filters.add(Query.of(q -> q.exists(e -> e.field("observation_id"))));
        }

        if(isoDate != null) {
            filters.add(Query.of(f -> f.
                    range(r -> r.
                            field("effective_time_frame").
                            to(isoDate))));
        }
        return filters;
    }

    private Map<String, Object> toData(Map<String, Object> source) {
        Map<String, Object> result = new HashMap<>();
        source.keySet().stream()
                .filter(k -> k.startsWith("data_"))
                .filter(k -> !k.equals("data_type"))
                .forEach(k -> result.put(k.substring(5), source.get(k)));
        return result;
    }

    static <R, T extends Exception> R handleIndexNotFoundException(T e, Supplier<R> supplier) throws T {
        return ElasticService.handleIndexNotFoundException(e, supplier, Function.identity());
    }

    static <R, E extends Exception, T extends Exception> R handleIndexNotFoundException(E e, Supplier<R> supplier, Function<E, T> exceptionTFunction) throws T {
        if (isElasticIndexNotFound(e)) return supplier.get();
        throw exceptionTFunction.apply(e);
    }

    static boolean isElasticIndexNotFound(Exception e) {
        if (e instanceof ElasticsearchException ee) {
            if (Objects.equals(ee.error().type(), "index_not_found_exception")) {
                LOG.debug("Swallowing Index-Not-Found from Elastic");
                return true;
            }
        }
        return false;
    }

    static <E extends Exception> void handleIndexNotFoundException(E e) throws E {
        handleIndexNotFoundException(e, Function.identity());
    }

    static <E extends Exception, T extends Exception> void handleIndexNotFoundException(E e, Function<E, T> exceptionWrapper) throws T {
        if (!isElasticIndexNotFound(e))
            throw exceptionWrapper.apply(e);
    }
}
