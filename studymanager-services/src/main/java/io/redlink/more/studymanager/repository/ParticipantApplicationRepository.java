/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.model.ParticipantApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public class ParticipantApplicationRepository {
    private static final String INSERT_OR_UPDATE =
            "INSERT INTO participant_applications (study_id, participant_id, application, uuid) " +
                    "VALUES (:study_id, :participant_id, :application, :uuid) " +
                    "ON CONFLICT (study_id, participant_id, application) DO UPDATE SET uuid = EXCLUDED.uuid " +
                    "RETURNING *";

    private static final String SELECT_BY_IDS =
            "SELECT * FROM participant_applications WHERE study_id = ? AND participant_id = ? AND application = ?";

    private static final String SELECT_BY_PARTICIPANT =
            "SELECT * FROM participant_applications WHERE study_id = ? AND participant_id = ?";

    private static final String SELECT_BY_STUDY =
            "SELECT * FROM participant_applications WHERE study_id = ?";

    private static final String SELECT_BY_STUDY_AND_APPLICATION =
            "SELECT * FROM participant_applications WHERE study_id = ? AND application = ?";

    private static final String DELETE_BY_PARTICIPANT_AND_APPLICATION =
            "DELETE FROM participant_applications WHERE study_id = ? AND participant_id = ? AND application = ?";

    private static final String DELETE_BY_PARTICIPANT =
            "DELETE FROM participant_applications WHERE study_id = ? AND participant_id = ?";

    private static final String DELETE_BY_STUDY =
            "DELETE FROM participant_applications WHERE study_id = ?";

    private static final String DELETE_BY_STUDY_EXCEPT_APPLICATIONS =
            "DELETE FROM participant_applications WHERE study_id = :study_id AND application NOT IN (:applications)";

    private final JdbcTemplate template;
    private final NamedParameterJdbcTemplate namedTemplate;

    public ParticipantApplicationRepository(JdbcTemplate template) {
        this.template = template;
        this.namedTemplate = new NamedParameterJdbcTemplate(template);
    }

    public ParticipantApplication save(ParticipantApplication participantApplication) {
        return namedTemplate.queryForObject(INSERT_OR_UPDATE,
                new MapSqlParameterSource()
                        .addValue("study_id", participantApplication.getStudyId())
                        .addValue("participant_id", participantApplication.getParticipantId())
                        .addValue("application", participantApplication.getApplication())
                        .addValue("uuid", participantApplication.getUuid()),
                getRowMapper());
    }

    public Optional<ParticipantApplication> findByIds(Long studyId, Integer participantId, String application) {
        return template.query(SELECT_BY_IDS, getRowMapper(), studyId, participantId, application)
                .stream().findFirst();
    }

    public List<ParticipantApplication> findAllByParticipant(Long studyId, Integer participantId) {
        return template.query(SELECT_BY_PARTICIPANT, getRowMapper(), studyId, participantId);
    }

    public List<ParticipantApplication> findAllByStudy(Long studyId) {
        return template.query(SELECT_BY_STUDY, getRowMapper(), studyId);
    }

    public List<ParticipantApplication> findAllByStudyAndApplication(Long studyId, String application) {
        return template.query(SELECT_BY_STUDY_AND_APPLICATION, getRowMapper(), studyId, application);
    }

    public void delete(Long studyId, Integer participantId, String application) {
        template.update(DELETE_BY_PARTICIPANT_AND_APPLICATION, studyId, participantId, application);
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

    private RowMapper<ParticipantApplication> getRowMapper() {
        return (rs, rowNum) -> new ParticipantApplication()
                .setStudyId(rs.getLong("study_id"))
                .setParticipantId(rs.getInt("participant_id"))
                .setApplication(rs.getString("application"))
                .setUuid(rs.getObject("uuid", UUID.class));
    }
}
