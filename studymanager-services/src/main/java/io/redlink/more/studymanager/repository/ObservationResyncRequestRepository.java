/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache License, Version 2.0.
 */
package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.exception.DataConstraintException;
import io.redlink.more.studymanager.model.ObservationResyncRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ObservationResyncRequestRepository {

    private static final String INSERT_RESYNC_REQUEST = """
            INSERT INTO observation_resync_requests (study_id, participant_id, observation_id)
            VALUES (:study_id, :participant_id, :observation_id)
            RETURNING *""";
    private static final String GET_RESYNC_REQUEST_BY_IDS = """
            SELECT * FROM observation_resync_requests
            WHERE study_id = ? AND participant_id = ? AND observation_id = ?""";
    private static final String LIST_RESYNC_REQUESTS_BY_PARTICIPANT = """
            SELECT * FROM observation_resync_requests
            WHERE study_id = ? AND participant_id = ?
            ORDER BY observation_id""";
    private static final String DELETE_RESYNC_REQUEST = """
            DELETE FROM observation_resync_requests
            WHERE study_id = ? AND participant_id = ? AND observation_id = ?""";
    private static final String CLEAR_RESYNC_REQUESTS = "DELETE FROM observation_resync_requests";

    private final JdbcTemplate template;
    private final NamedParameterJdbcTemplate namedTemplate;

    public ObservationResyncRequestRepository(JdbcTemplate template) {
        this.template = template;
        this.namedTemplate = new NamedParameterJdbcTemplate(template);
    }

    public ObservationResyncRequest insert(long studyId, int participantId, int observationId) {
        try {
            return namedTemplate.queryForObject(
                    INSERT_RESYNC_REQUEST,
                    new MapSqlParameterSource()
                            .addValue("study_id", studyId)
                            .addValue("participant_id", participantId)
                            .addValue("observation_id", observationId),
                    getRowMapper());
        } catch (DuplicateKeyException e) {
            throw DataConstraintException.createObservationResyncRequestAlreadyExists(studyId, participantId, observationId);
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException(
                    "Participant " + participantId + " or observation " + observationId +
                            " does not exist in study " + studyId);
        }
    }

    public Optional<ObservationResyncRequest> find(long studyId, int participantId, int observationId) {
        return template.query(GET_RESYNC_REQUEST_BY_IDS, getRowMapper(), studyId, participantId, observationId)
                .stream().findFirst();
    }

    public List<ObservationResyncRequest> listByParticipant(long studyId, int participantId) {
        return template.query(LIST_RESYNC_REQUESTS_BY_PARTICIPANT, getRowMapper(), studyId, participantId);
    }

    public void deleteByIds(long studyId, int participantId, int observationId) {
        template.update(DELETE_RESYNC_REQUEST, studyId, participantId, observationId);
    }

    private static RowMapper<ObservationResyncRequest> getRowMapper() {
        return (rs, rowNum) -> new ObservationResyncRequest(
                rs.getLong("study_id"),
                rs.getInt("participant_id"),
                rs.getInt("observation_id"),
                RepositoryUtils.readInstant(rs, "created"));
    }

    // for testing purpose only
    protected void clear() {
        template.execute(CLEAR_RESYNC_REQUESTS);
    }
}
