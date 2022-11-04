package io.redlink.more.studymanager.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "elastic")
public class ElasticProperties {
    private String host;
    private int port;
    private String numberOfShards;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getNumberOfShards() {
        return numberOfShards;
    }

    public void setNumberOfShards(String numberOfShards) {
        this.numberOfShards = numberOfShards;
    }
}
