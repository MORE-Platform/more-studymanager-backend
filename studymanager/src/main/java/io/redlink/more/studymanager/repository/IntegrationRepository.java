package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.model.EndpointToken;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IntegrationRepository {
//TODO reg_token anschauen wegen verchlüsselung
    private static final String ADD_TOKEN =
            "INSERT INTO observation_api_tokens(study_id, observation_id, token_label, token) " +
            "VALUES (:study_id, :observation_id, :token_label, :token) " +
            "ON CONFLICT (study_id, observation_id, token_label) DO NOTHING " +
            "RETURNING *";
    private static final String LIST_TOKENS =
            "SELECT token_id, token_label, token, created " +
            "FROM observation_api_tokens " +
            "WHERE study_id = ? AND observation_id = ?";
    private static final String GET_TOKEN =
            "SELECT token_id, token_label, token, created " +
            "FROM observation_api_tokens " +
            "WHERE study_id = ? AND observation_id = ? AND token_id = ?";
    private static final String DELETE_TOKEN =
            "DELETE FROM observation_api_tokens " +
            "WHERE study_id = ? AND observation_id = ? AND token_id = ?";
    private static final String DELETE_ALL = "DELETE FROM observation_api_tokens";

    private final JdbcTemplate template;

    private final NamedParameterJdbcTemplate namedTemplate;

    public IntegrationRepository(JdbcTemplate template) {
        this.template = template;
        this.namedTemplate = new NamedParameterJdbcTemplate(template);
    }

    public void clear() { template.execute(DELETE_ALL);}

    public EndpointToken addToken(Long studyId, Integer observationId, EndpointToken token) {
        try {
            return namedTemplate.queryForObject(ADD_TOKEN,
                    new MapSqlParameterSource()
                            .addValue("token_label", token.getTokenLabel())
                            .addValue("token", token.getToken())
                            .addValue("study_id", studyId)
                            .addValue("observation_id", observationId),
                    getTokenRowMapper());
        } catch (EmptyResultDataAccessException e) {
            throw new BadRequestException("Token with given token label already exists for given observation");
        }
    }

    public List<EndpointToken> getAllTokens(Long studyId, Integer observationId) {
        return template.query(LIST_TOKENS, getTokenRowMapper(), studyId, observationId);
    }

    public EndpointToken getToken(Long studyId, Integer observationId, Integer tokenId) {
        try {
            return template.queryForObject(GET_TOKEN, getTokenRowMapper(), studyId, observationId, tokenId);
        } catch(EmptyResultDataAccessException e) {
            throw new BadRequestException("Token with id " + tokenId + " does not exist");
        }
    }

    public void deleteToken(Long studyId, Integer observationId, Integer tokenId) {
        template.update(DELETE_TOKEN, studyId, observationId, tokenId);
    }

    private static RowMapper<EndpointToken> getTokenRowMapper() {
        return (rs, rowNum) -> new EndpointToken()
                .setTokenId(rs.getInt("token_id"))
                .setTokenLabel(rs.getString("token_label"))
                .setToken(rs.getString("token"))
                .setCreated(RepositoryUtils.readInstant(rs, "created"));
    }
}
