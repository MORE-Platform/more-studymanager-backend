/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.configuration;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.redlink.more.studymanager.properties.ElasticProperties;
import java.net.URI;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
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
    public ElasticsearchClient elasticServiceClient(ObjectMapper objectMapper) {
        // Create the low-level client
        final HttpHost elasticHost;
        if (elasticProperties.uri() != null) {
            final URI uri = elasticProperties.uri();
            elasticHost = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
        } else {
            elasticHost = new HttpHost(elasticProperties.host(), elasticProperties.port());
        }

        final RestClientBuilder clientBuilder = RestClient.builder(elasticHost);
        if (StringUtils.isNotBlank(elasticProperties.username())) {
            final var credentialProvider = new BasicCredentialsProvider();
            credentialProvider.setCredentials(
                    new AuthScope(elasticHost),
                    new UsernamePasswordCredentials(elasticProperties.username(), elasticProperties.password())
            );
            clientBuilder.setHttpClientConfigCallback(cb -> cb.setDefaultCredentialsProvider(credentialProvider));
        }

        final RestClient restClient = clientBuilder.build();

        // Create the transport with a Jackson mapper
        final ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper(objectMapper));

        // And create the API client
        return new ElasticsearchClient(transport);
    }

}
