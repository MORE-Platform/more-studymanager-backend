/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.model.LoginToken;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class LoginTokenRepository {
    private static final String INSERT_TOKEN =
            "INSERT INTO login_tokens (study_id, participant_id, application, code, code_hash) " +
                    "VALUES (:study_id, :participant_id, :application, :code, :code_hash) " +
                    "ON CONFLICT (study_id, participant_id, application) DO UPDATE SET code = EXCLUDED.code, code_hash = EXCLUDED.code_hash " +
                    "RETURNING *";

    private static final String SELECT_BY_IDS =
            "SELECT * FROM login_tokens WHERE study_id = ? AND participant_id = ? AND application = ?";

    private static final String SELECT_BY_PARTICIPANT =
            "SELECT * FROM login_tokens WHERE study_id = ? AND participant_id = ?";

    private static final String SELECT_BY_STUDY_AND_APPLICATION =
            "SELECT * FROM login_tokens WHERE study_id = ? AND application = ?";

    private static final String DELETE_BY_STUDY_AND_APPLICATION =
            "DELETE FROM login_tokens WHERE study_id = ? AND application = ?";

    private static final String DELETE_BY_PARTICIPANT =
            "DELETE FROM login_tokens WHERE study_id = ? AND participant_id = ?";

    private static final String DELETE_BY_STUDY =
            "DELETE FROM login_tokens WHERE study_id = ?";

    private static final String DELETE_TOKEN =
            "DELETE FROM login_tokens WHERE study_id = ? AND participant_id = ? AND application = ?";

    private static final String DELETE_BY_STUDY_EXCEPT_APPLICATIONS =
            "DELETE FROM login_tokens WHERE study_id = :study_id AND application NOT IN (:applications)";

    private final JdbcTemplate template;
    private final NamedParameterJdbcTemplate namedTemplate;

    public LoginTokenRepository(JdbcTemplate template) {
        this.template = template;
        this.namedTemplate = new NamedParameterJdbcTemplate(template);
    }

    public LoginToken save(LoginToken token) {
        return namedTemplate.queryForObject(INSERT_TOKEN,
                new MapSqlParameterSource()
                        .addValue("study_id", token.getStudyId())
                        .addValue("participant_id", token.getParticipantId())
                        .addValue("application", token.getApplication())
                        .addValue("code", token.getCode())
                        .addValue("code_hash", token.getCodeHash()),
                getRowMapper());
    }

    public Optional<LoginToken> findByIds(Long studyId, Integer participantId, String application) {
        return template.query(SELECT_BY_IDS, getRowMapper(), studyId, participantId, application)
                .stream().findFirst();
    }

    public List<LoginToken> findAllByParticipant(Long studyId, Integer participantId) {
        return template.query(SELECT_BY_PARTICIPANT, getRowMapper(), studyId, participantId);
    }

    public List<LoginToken> findAllByStudyAndApplication(Long studyId, String application) {
        return template.query(SELECT_BY_STUDY_AND_APPLICATION, getRowMapper(), studyId, application);
    }

    public void deleteAllByStudyAndApplication(Long studyId, String application) {
        template.update(DELETE_BY_STUDY_AND_APPLICATION, studyId, application);
    }

    public void deleteAllByParticipant(Long studyId, Integer participantId) {
        template.update(DELETE_BY_PARTICIPANT, studyId, participantId);
    }

    public void deleteAllByStudy(Long studyId) {
        template.update(DELETE_BY_STUDY, studyId);
    }

    public void deleteAllByStudyExcept(Long studyId, Set<String> applications) {
        if (applications.isEmpty()) {
            deleteAllByStudy(studyId);
        } else {
            namedTemplate.update(DELETE_BY_STUDY_EXCEPT_APPLICATIONS,
                    new MapSqlParameterSource()
                            .addValue("study_id", studyId)
                            .addValue("applications", applications));
        }
    }

    public void delete(Long studyId, Integer participantId, String application) {
        template.update(DELETE_TOKEN, studyId, participantId, application);
    }

    private RowMapper<LoginToken> getRowMapper() {
        return (rs, rowNum) -> new LoginToken()
                .setStudyId(rs.getLong("study_id"))
                .setParticipantId(rs.getInt("participant_id"))
                .setApplication(rs.getString("application"))
                .setCode(rs.getString("code"))
                .setCodeHash(rs.getString("code_hash"));
    }
}
