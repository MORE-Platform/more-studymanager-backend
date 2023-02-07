package io.redlink.more.studymanager.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.json.JsonData;
import com.google.common.io.Resources;
import io.redlink.more.studymanager.configuration.ElasticConfiguration;
import io.redlink.more.studymanager.model.ElasticDataPoint;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.core.io.Timeframe;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@ContextConfiguration(initializers = ElasticSearchServiceTest.EnvInitializer.class,
        classes = {
                ElasticService.class,
                ElasticConfiguration.class,
                JacksonAutoConfiguration.class,
        })
class ElasticSearchServiceTest {

    @Container
    public static ElasticsearchContainer elasticContainer = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:8.3.2")
            .withEnv("xpack.security.enabled", "false")
            .withEnv("discovery.type", "single-node")
            .withEnv("action.auto_create_index", "true")
            .withEnv("bootstrap.memory_lock", "true")
            .withEnv("ES_JAVA_OPTS", "-Xms256m -Xmx512m");

    @MockBean
    private DataProcessingService dataProcessingService;

    @Autowired
    private ElasticService elasticService;

    @Autowired
    private ElasticsearchClient client;

    @Test
    void testParticipantsThatMapQuery() throws InterruptedException {
        Study study = new Study().setStudyId(28L);

        indexDoc(study, 1);
        indexDoc(study, 2);

        Thread.sleep(1000);

        List<Integer> ids = elasticService.participantsThatMapQuery(
                study.getStudyId(), null, "data_z:[* TO *]",
                new Timeframe(Instant.parse("2022-10-24T10:00:00Z"), Instant.parse("2022-10-24T15:00:00Z"))
        );
        assertThat(ids).hasSize(1);
        assertThat(ids.get(0)).isEqualTo(10);

        elasticService.deleteIndex(study);
    }

    @Test
    void testRecordAction() {
        assertThatNoException().isThrownBy(() ->
                elasticService.setDataPoint(25L, new ElasticDataPoint(
                        UUID.randomUUID().toString(),
                        "participant_1",
                        "study_25",
                        "study_group_1",
                        "action_1",
                        "test",
                        "test",
                        Instant.now(),
                        Instant.now(),
                        Map.of()
                ))
        );
    }

    @Test
    void testGetParticipationData() throws InterruptedException{
        Study study = new Study().setStudyId(30L);
        indexDoc(study,3);
        indexDoc(study,4);
        indexDoc(study,5);
        indexDoc(study,6);
        indexDoc(study,7);
        indexDoc(study,8);

        Thread.sleep(1000);

        when(dataProcessingService.completeParticipationData(anyList(), anyLong())).thenAnswer(i -> {return i.getArgument(0);});

        assertThat(elasticService.getParticipationData(30L).size()).isEqualTo(5);
        System.out.println(elasticService.getParticipationData(30L).toString());
        elasticService.deleteIndex(study);
    }

    private void indexDoc(Study study, int i) {
        IndexRequest<JsonData> request = IndexRequest.of(d -> d
                .index(ElasticService.getStudyIdString(study))
                .id(String.valueOf(i))
                .withJson(file(i)));
        try {
            client.index(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream file(int i) {
        try {
            return Resources.getResource("elastic/doc" + i + ".json").openStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static class EnvInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of(
                    String.format("elastic.port=%d", elasticContainer.getFirstMappedPort())
            ).applyTo(applicationContext);
        }
    }
}
