package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.model.EndpointToken;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IntegrationRepository {

    private static final String ADD_TOKEN =
            "INSERT INTO observation_api_tokens(study_id, observation_id, token_id, token_label, token) " +
            "VALUES (:study_id, :observation_id, :token_id, :token_label, :token) " +
            "ON CONFLICT (study_id, observation_id) DO NOTHING " +
            "RETURNING *";
    private static final String LIST_TOKENS =
            "SELECT token_id, token_label, token, created " +
            "FROM observation_api_tokens " +
            "WHERE study_id = ? AND observation_id = ?";
    private static final String GET_TOKEN =
            "SELECT token_id, token_label, token, created " +
            "FROM observation_api_tokens " +
            "WHERE study_id = ? AND observation_id = ? AND token_label = ?";
    private static final String DELETE_TOKEN =
            "DELETE FROM observation_api_tokens " +
            "WHERE study_id = ? AND observation_id = ? AND token_label = ?";

    private final JdbcTemplate template;

    private final NamedParameterJdbcTemplate namedTemplate;

    public IntegrationRepository(JdbcTemplate template) {
        this.template = template;
        this.namedTemplate = new NamedParameterJdbcTemplate(template);
    }

    public EndpointToken addToken(Long studyId, Integer observationId, EndpointToken token) {
        return namedTemplate.queryForObject(ADD_TOKEN,
                tokenToParams(token)
                        .addValue("study_id", studyId)
                        .addValue("observation_id", observationId),
                getTokenRowMapper());
    }

    public List<EndpointToken> getAllTokens(Long studyId, Integer observationId) {
        return template.query(LIST_TOKENS, getTokenRowMapper(), studyId, observationId);
    }

    public EndpointToken getToken(Long studyId, Integer observationId, String tokenLabel) {
        return template.queryForObject(GET_TOKEN, getTokenRowMapper(), studyId, observationId, tokenLabel);
    }

    public void deleteToken(Long studyId, Integer observationId, String tokenLabel) {
        template.update(DELETE_TOKEN, studyId, observationId, tokenLabel);
    }

    private static RowMapper<EndpointToken> getTokenRowMapper() {
        return (rs, rowNum) -> new EndpointToken()
                .setTokenId(rs.getInt("token_id"))
                .setTokenLabel(rs.getString("token_label"))
                .setToken(rs.getString("token"))
                .setCreated(RepositoryUtils.readInstant(rs, "created"));
    }

    private static MapSqlParameterSource tokenToParams(EndpointToken token) {
        return new MapSqlParameterSource()
                .addValue("token_id", token.getTokenId())
                .addValue("token_label", token.getTokenLabel())
                .addValue("token", token.getToken());
    }
}
