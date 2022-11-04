package io.redlink.more.studymanager.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.*;
import co.elastic.clients.util.ObjectBuilder;
import io.redlink.more.studymanager.configuration.ElasticConfiguration;
import io.redlink.more.studymanager.properties.ElasticProperties;
import io.redlink.more.studymanager.model.Study;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@EnableConfigurationProperties({ElasticProperties.class})
public class ElasticService {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticService.class);

    private final ElasticProperties elasticProperties;
    private final ElasticsearchClient client;

    public ElasticService(ElasticConfiguration elasticConfiguration, ElasticProperties elasticProperties) {
        this.elasticProperties = elasticProperties;
        this.client = elasticConfiguration.elasticServiceClient();
    }

    public boolean createIndex(Study study) {
        try {
            IndexSettings settings = new IndexSettings.Builder()
                    .numberOfShards(this.elasticProperties.getNumberOfShards())
                    .build();
            CreateIndexRequest indexRequest = new CreateIndexRequest.Builder()
                    .index(study.getStudyId().toString())
                    .settings(settings)
                    .mappings(this::createMapping)
                    .build();

            this.client.indices().create(indexRequest);
            return true;
        } catch (IOException | ElasticsearchException e) {
            LOG.warn("Error when creating elastic index. Error message: ", e);
            return false;
        }
    }

    private ObjectBuilder<TypeMapping> createMapping(TypeMapping.Builder mapping) {
        return mapping;
    }

    public boolean closeIndex(Study study) {
        try {
            CloseIndexRequest indexRequest = new CloseIndexRequest.Builder()
                    .index(study.getStudyId().toString())
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
                    .index(study.getStudyId().toString())
                    .ignoreUnavailable(true)
                    .build();
            this.client.indices().delete(indexRequest);
            return true;
        } catch (IOException | ElasticsearchException e) {
            LOG.warn("Error when deleting elastic index. Error message: ", e);
            return false;
        }
    }

}
