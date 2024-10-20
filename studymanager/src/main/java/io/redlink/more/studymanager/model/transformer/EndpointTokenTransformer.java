/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.EndpointTokenDTO;
import io.redlink.more.studymanager.model.EndpointToken;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class EndpointTokenTransformer {

    private EndpointTokenTransformer() {}

    public static EndpointToken fromEndpointTokenDTO(EndpointTokenDTO dto) {
        Instant offsetDateTime = dto.getCreated();
        return new EndpointToken(
                dto.getTokenId(),
                dto.getTokenLabel(),
                offsetDateTime,
                dto.getToken()
        );
    }

    public static List<EndpointToken> fromEndpointTokensDTO(Collection<EndpointTokenDTO> dto) {
        if(dto == null) {
            return List.of();
        }
        return dto.stream()
                .map(EndpointTokenTransformer::fromEndpointTokenDTO)
                .filter(Objects::nonNull)
                .toList();
    }

    public static EndpointTokenDTO toEndpointTokenDTO(EndpointToken token) {
        return new EndpointTokenDTO()
                .tokenId(token.tokenId())
                .tokenLabel(token.tokenLabel())
                .created(token.created())
                .token(token.token());
    }

    public static List<EndpointTokenDTO> toEndpointTokensDTO(Collection<EndpointToken> tokens) {
        if(tokens == null) {
            return List.of();
        }
        return tokens.stream()
                .map(EndpointTokenTransformer::toEndpointTokenDTO)
                .filter(Objects::nonNull)
                .toList();
    }
}
