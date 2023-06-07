package io.redlink.more.studymanager.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Conflicts;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.DeleteByQueryRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.indices.CloseIndexRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import io.redlink.more.studymanager.core.io.Timeframe;
import io.redlink.more.studymanager.model.data.ElasticActionDataPoint;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.ParticipationData;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.data.ElasticDataPoint;
import io.redlink.more.studymanager.properties.ElasticProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@EnableConfigurationProperties({ElasticProperties.class})
public class ElasticService {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticService.class);

    private final ElasticsearchClient client;

    public ElasticService(ElasticsearchClient client) { this.client = client; }

    public List<Integer> participantsThatMapQuery(Long studyId, Integer studyGroupId, String query, Timeframe timeframe) {
        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder
                .index(getStudyIdString(studyId))
                .size(0)
                .query(q -> q.
                        bool(b -> b.
                                must(m -> m.
                                        queryString(qs -> qs.
                                                query(query))).
                                filter(getFilters(studyId, studyGroupId, timeframe))
                        )).aggregations(
                        "participant_ids",
                        a -> a.terms(t -> t.
                                field("participant_id.keyword").
                                size(10000))
                );

        try {
            return client.search(builder.build(), Map.class)
                    .aggregations().get("participant_ids").sterms().buckets().array().stream()
                    .map(StringTermsBucket::key)
                    .map(FieldValue::stringValue)
                    .map(s -> s.substring(12))
                    .map(Integer::valueOf)
                    .toList();
        } catch (IOException | ElasticsearchException e) {
            LOG.error("Elastic Query failed", e);
            return List.of();
        }
    }

    private List<Query> getFilters(Long studyId, Integer studyGroupId, Timeframe timeframe) {
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

        if (timeframe != null && timeframe.getFrom() != null && timeframe.getTo() != null) {
            queries.add(Query.of(f -> f.
                    range(r -> r.
                            field("effective_time_frame").
                            from(timeframe.getFrom().toString()).
                            to(timeframe.getTo().toString())
                    )));
        }
        return queries;
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
        try {
            DeleteIndexRequest indexRequest = new DeleteIndexRequest.Builder()
                    .index(getStudyIdString(study))
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



}
