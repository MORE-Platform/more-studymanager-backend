/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.model.SaltToken;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class SaltTokenRepository {

    private static final String INSERT_SALT =
            "INSERT INTO salt_tokens (study_id, participant_id, salt) " +
                    "VALUES (:study_id, :participant_id, :salt) " +
                    "ON CONFLICT (study_id, participant_id) DO UPDATE SET salt = EXCLUDED.salt " +
                    "RETURNING *";

    private static final String SELECT_BY_IDS =
            "SELECT * FROM salt_tokens WHERE study_id = ? AND participant_id = ?";

    private static final String DELETE_SALT =
            "DELETE FROM salt_tokens WHERE study_id = ? AND participant_id = ?";

    private static final String DELETE_BY_STUDY =
            "DELETE FROM salt_tokens WHERE study_id = ?";

    private final JdbcTemplate template;
    private final NamedParameterJdbcTemplate namedTemplate;

    public SaltTokenRepository(JdbcTemplate template) {
        this.template = template;
        this.namedTemplate = new NamedParameterJdbcTemplate(template);
    }

    public SaltToken save(SaltToken saltToken) {
        return namedTemplate.queryForObject(INSERT_SALT,
                new MapSqlParameterSource()
                        .addValue("study_id", saltToken.getStudyId())
                        .addValue("participant_id", saltToken.getParticipantId())
                        .addValue("salt", saltToken.getSalt()),
                getRowMapper());
    }

    public Optional<SaltToken> findByIds(Long studyId, Integer participantId) {
        return template.query(SELECT_BY_IDS, getRowMapper(), studyId, participantId)
                .stream().findFirst();
    }

    public void delete(Long studyId, Integer participantId) {
        template.update(DELETE_SALT, studyId, participantId);
    }

    public void deleteAllByStudy(Long studyId) {
        template.update(DELETE_BY_STUDY, studyId);
    }

    private RowMapper<SaltToken> getRowMapper() {
        return (rs, rowNum) -> new SaltToken()
                .setStudyId(rs.getLong("study_id"))
                .setParticipantId(rs.getInt("participant_id"))
                .setSalt(rs.getString("salt"));
    }
}
