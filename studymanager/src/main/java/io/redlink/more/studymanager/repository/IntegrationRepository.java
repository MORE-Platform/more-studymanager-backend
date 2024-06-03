/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.model.EndpointToken;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class IntegrationRepository {
    private static final String ADD_TOKEN =
            "INSERT INTO observation_api_tokens(study_id, observation_id, token_id, token_label, token) " +
            "VALUES (:study_id, :observation_id, (SELECT COALESCE(MAX(token_id),0)+1 FROM observation_api_tokens WHERE study_id = :study_id AND observation_id = :observation_id), :token_label, :token) " +
            "RETURNING *";
    private static final String LIST_TOKENS =
            "SELECT token_id, token_label, created " +
            "FROM observation_api_tokens " +
            "WHERE study_id = ? AND observation_id = ?";
    private static final String GET_TOKEN =
            "SELECT token_id, token_label, created " +
            "FROM observation_api_tokens " +
            "WHERE study_id = ? AND observation_id = ? AND token_id = ?";
    private static final String DELETE_TOKEN =
            "DELETE FROM observation_api_tokens " +
            "WHERE study_id = ? AND observation_id = ? AND token_id = ?";
    private static final String DELETE_ALL = "DELETE FROM observation_api_tokens";
    private static final String UPDATE_TOKEN = """
            UPDATE observation_api_tokens
            SET token_label = :token_label
            WHERE study_id = :study_id AND observation_id = :observation_id AND token_id = :token_id
            RETURNING token_id, token_label, created
            """;
    private static final String DELETE_ALL_FOR_STUDY_ID =
            "DELETE FROM observation_api_tokens " +
            "WHERE study_id = ?";

    private final JdbcTemplate template;

    private final NamedParameterJdbcTemplate namedTemplate;

    public IntegrationRepository(JdbcTemplate template) {
        this.template = template;
        this.namedTemplate = new NamedParameterJdbcTemplate(template);
    }

    public void clear() { template.execute(DELETE_ALL);}

    public void clearForStudyId(long studyId) {
        template.update(DELETE_ALL_FOR_STUDY_ID, studyId);
    }

    public Optional<EndpointToken> addToken(Long studyId, Integer observationId, String tokenLabel, String encryptedSecret) {
        try {
            return Optional.ofNullable(namedTemplate.queryForObject(ADD_TOKEN,
                    new MapSqlParameterSource()
                            .addValue("token_label", tokenLabel)
                            .addValue("token", encryptedSecret)
                            .addValue("study_id", studyId)
                            .addValue("observation_id", observationId),
                    getHiddenTokenRowMapper()));
        } catch(DuplicateKeyException e) {
            return Optional.empty();
        }
    }

    public List<EndpointToken> getAllTokens(Long studyId, Integer observationId) {
        return template.query(LIST_TOKENS, getHiddenTokenRowMapper(), studyId, observationId);
    }

    public Optional<EndpointToken> getToken(Long studyId, Integer observationId, Integer tokenId) {
        try {
            return Optional.ofNullable(template.queryForObject(GET_TOKEN, getHiddenTokenRowMapper(), studyId, observationId, tokenId));
        } catch(EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void deleteToken(Long studyId, Integer observationId, Integer tokenId) {
        template.update(DELETE_TOKEN, studyId, observationId, tokenId);
    }

    public Optional<EndpointToken> updateToken(Long studyId, Integer observationId, Integer tokenId, String tokenLabel) {
        try {
            return Optional.ofNullable(
                    namedTemplate.queryForObject(UPDATE_TOKEN,
                            new MapSqlParameterSource()
                                    .addValue("study_id", studyId)
                                    .addValue("observation_id", observationId)
                                    .addValue("token_id", tokenId)
                                    .addValue("token_label", tokenLabel),
                            getHiddenTokenRowMapper()
                    )
            );
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private static RowMapper<EndpointToken> getHiddenTokenRowMapper() {
        return (rs, rowNum) -> new EndpointToken(
                rs.getInt("token_id"),
                rs.getString("token_label"),
                RepositoryUtils.readInstant(rs, "created"),
                null
        );
    }
}
