package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.EndpointTokenDTO;
import io.redlink.more.studymanager.model.EndpointToken;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class EndpointTokenTransformer {

    private EndpointTokenTransformer() {}

    public static EndpointToken fromEndpointTokenDTO(EndpointTokenDTO dto) {
        return new EndpointToken()
                .setTokenId(dto.getTokenId())
                .setTokenLabel(dto.getTokenLabel())
                .setCreated(Transformers.toInstant(dto.getCreated()))
                .setToken(dto.getToken());
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
                .tokenId(token.getTokenId())
                .tokenLabel(token.getTokenLabel())
                .created(Transformers.toOffsetDateTime(token.getCreated()))
                .token(token.getToken());
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
