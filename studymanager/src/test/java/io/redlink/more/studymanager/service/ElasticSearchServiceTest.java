package io.redlink.more.studymanager.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.json.JsonData;
import com.google.common.io.Resources;
import io.redlink.more.studymanager.configuration.ElasticConfiguration;
import io.redlink.more.studymanager.model.Study;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@ContextConfiguration(initializers = ElasticSearchServiceTest.EnvInitializer.class,
        classes = {
                ElasticService.class,
                ElasticConfiguration.class
        })
public class ElasticSearchServiceTest {

    @Container
    public static ElasticsearchContainer elasticContainer = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:8.3.2")
            .withEnv("xpack.security.enabled", "false")
            .withEnv("discovery.type", "single-node")
            .withEnv("action.auto_create_index", "true")
            .withEnv("bootstrap.memory_lock", "true")
            .withEnv("ES_JAVA_OPTS", "-Xms256m -Xmx512m");

    @Autowired
    private ElasticService elasticService;

    @Autowired
    private ElasticsearchClient client;

    @Test
    public void testParticipantsThatMapQuery() throws InterruptedException {
        Study study = new Study().setStudyId(28L);

        indexDoc(study, 1);
        indexDoc(study, 2);

        Thread.sleep(1000);

        List<Integer> ids = elasticService.participantsThatMapQuery(
                study.getStudyId(), null, "data_z:[* TO *]"
        );
        assertThat(ids).hasSize(1);
        assertThat(ids.get(0)).isEqualTo(10);

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
            return Resources.getResource("elastic/doc"+i+".json").openStream();
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
