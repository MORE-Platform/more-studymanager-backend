package io.redlink.more.studymanager.model;

import java.time.Instant;

public record EndpointToken(
    Integer tokenId,
    String tokenLabel,
    Instant created,
    String token
) {
    public EndpointToken(String tokenLabel, String token) {
        this(Integer.MIN_VALUE, tokenLabel, null, token);
    }
}
