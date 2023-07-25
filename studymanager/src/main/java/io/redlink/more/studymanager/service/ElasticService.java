package io.redlink.more.studymanager.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Conflicts;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CloseIndexRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Iterables;
import io.redlink.more.studymanager.core.io.TimeRange;
import io.redlink.more.studymanager.model.ParticipationData;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.data.ElasticDataPoint;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private List<Query> getFilters(Long studyId, Integer studyGroupId, TimeRange timerange) {
        List<Query> queries = new ArrayList<>();
        queries.add(Query.of(f -> f.
                term(t -> t.
                        field("study_id").
                        value(getStudyIdString(studyId)))));
        if (studyGroupId != null) {
            queries.add(Query.of(f -> f.term(t -> t.
                    field("study_group_id").
                    value(getStudyGroupIdString(studyGroupId)))));
        }

        if (timerange != null && timerange.getFromString() != null && timerange.getToString() != null) {
            queries.add(Query.of(f -> f.
                    range(r -> r.
                            field("effective_time_frame").
                            from(timerange.getFromString()).
                            to(timerange.getToString())
                    )));
        }
        return queries;
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
            LOG.warn("Error when deleting participant from elastic index. Error message: ", e);
            throw new RuntimeException(e);
        }
    }

    static String getStudyIdString(Study study) {
        return getStudyIdString(study.getStudyId());
    }

    private String getStudyGroupIdString(Integer studyGroupId) {
        return "study_group_" + studyGroupId;
    }

    static String getStudyIdString(Long id) {
        return "study_" + id;
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
            LOG.error("Elastic Query failed", e);
            return new ArrayList<>();
        }
    }

    public void exportData(Long studyId, OutputStream outputStream) throws IOException {
        String index = getStudyIdString(studyId);

        //OpenPointInTimeResponse pitRsp = client.openPointInTime(r -> r.index(index).keepAlive(k -> k.time("1m")));
        String pitId = "";//pitRsp.id();

        SearchRequest request = getQuery(index, pitId, null);
        SearchResponse<JsonNode> rsp = client.search(request, JsonNode.class);

        while (rsp.hits().hits().size() > 0) {
            writeHits(rsp.hits().hits(), outputStream);
            outputStream.flush();
            //pitId = rsp.pitId();
            List<FieldValue> searchAfterSort = Iterables.getLast(rsp.hits().hits()).sort();
            request = getQuery(index, pitId, searchAfterSort);
            rsp = client.search(request, JsonNode.class);
            if(rsp.hits().hits().size() > 0) {
                outputStream.write(",".getBytes(StandardCharsets.UTF_8));
            }
        }

        //client.closePointInTime(new ClosePointInTimeRequest.Builder().id(pitId).build());
    }

    private void writeHits(List<Hit<JsonNode>> hits, OutputStream outputStream) throws IOException {
        String datapoints = hits.stream()
                .map(Hit::source)
                .map(MapperUtils::writeValueAsString)
                .collect(Collectors.joining(","));
        outputStream.write(datapoints.getBytes(StandardCharsets.UTF_8));
    }

    private SearchRequest getQuery(String index, String pitId, List<FieldValue> searchAfterSort) {
        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder.index(index)
                .query(q -> q.matchAll(m -> m))
                //.pit(p -> p.id(pitId).keepAlive(k -> k.time("1m")))
                .sort(s -> s.field(f -> f.field("effective_time_frame").order(SortOrder.Asc)))
                .size(BATCH_SIZE_FOR_EXPORT_REQUESTS);

        if(searchAfterSort != null) {
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
}
