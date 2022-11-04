package io.redlink.more.studymanager.configuration;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import io.redlink.more.studymanager.properties.ElasticProperties;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({ElasticProperties.class})
public class ElasticConfiguration {

    private final ElasticProperties elasticProperties;

    ElasticConfiguration(ElasticProperties elasticProperties) {
        this.elasticProperties = elasticProperties;
    }

    @Bean
    public ElasticsearchClient elasticServiceClient(){
        // Create the low-level client
        RestClient restClient = RestClient.builder(
                new HttpHost(elasticProperties.getHost(), elasticProperties.getPort())).build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        // And create the API client
        return new ElasticsearchClient(transport);
    }

}
