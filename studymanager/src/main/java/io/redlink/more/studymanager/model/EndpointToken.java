package io.redlink.more.studymanager.model;

import java.time.Instant;

public class EndpointToken {
    private Integer tokenId;
    private String tokenLabel;
    private Instant created;
    private String token;

    public Integer getTokenId() { return tokenId; }

    public EndpointToken setTokenId(Integer tokenId) {
        this.tokenId = tokenId;
        return this;
    }

    public String getTokenLabel() {
        return tokenLabel;
    }

    public EndpointToken setTokenLabel(String tokenLabel) {
        this.tokenLabel = tokenLabel;
        return this;
    }

    public Instant getCreated() {
        return created;
    }

    public EndpointToken setCreated(Instant created) {
        this.created = created;
        return this;
    }

    public String getToken() {
        return token;
    }

    public EndpointToken setToken(String token) {
        this.token = token;
        return this;
    }
}
