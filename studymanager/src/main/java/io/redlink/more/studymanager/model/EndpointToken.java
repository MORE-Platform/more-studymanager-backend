package io.redlink.more.studymanager.model;

import java.time.Instant;

public record EndpointToken(
    Integer tokenId,
    String tokenLabel,
    Instant created,
    String token
) {


    public EndpointToken withToken(String secret) {
        return new EndpointToken(
                tokenId,
                tokenLabel,
                created,
                secret
        );
    }
}
