package io.redlink.more.studymanager.configuration;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
public class JPAConfiguration {
    @Bean
    @Profile("test-containers-flyway")
    public DataSource dataSource()
    {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.url("jdbc:tc:postgresql:15-alpine:///test_database");
        dataSourceBuilder.username("user");
        dataSourceBuilder.password("password");
        return dataSourceBuilder.build();
    }
}
