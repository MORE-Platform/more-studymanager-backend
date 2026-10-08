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
import io.redlink.more.studymanager.model.ObservationResyncRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ObservationResyncRequestRepository {

    /**
     * An operator request is retried with high priority (every 5 minutes) for the next hour. If a request already exists it is merged:
     * it restarts now, keeps the later end and the faster interval of a still running request.
     */
    private static final String UPSERT_RESYNC_REQUEST = """
            INSERT INTO observation_resync_requests AS r
                (study_id, participant_id, observation_id, resync_interval, resync_start, resync_end)
            VALUES (:study_id, :participant_id, :observation_id, 'high', now(), now() + interval '1 hour')
            ON CONFLICT (study_id, participant_id, observation_id) DO UPDATE SET
                resync_start = now(),
                resync_end = GREATEST(r.resync_end, EXCLUDED.resync_end),
                resync_interval = CASE WHEN r.resync_end > now()
                        AND array_position(ARRAY['urgent','high','normal','low'], r.resync_interval)
                          < array_position(ARRAY['urgent','high','normal','low'], EXCLUDED.resync_interval)
                    THEN r.resync_interval ELSE EXCLUDED.resync_interval END,
                synced = FALSE
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
    private static final String LIST_SYNCED_RESYNC_REQUESTS =
            "SELECT * FROM observation_resync_requests WHERE synced ORDER BY created";
    private static final String DELETE_SYNCED_RESYNC_REQUEST = """
            DELETE FROM observation_resync_requests
            WHERE study_id = ? AND participant_id = ? AND observation_id = ? AND synced""";
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
                    UPSERT_RESYNC_REQUEST,
                    new MapSqlParameterSource()
                            .addValue("study_id", studyId)
                            .addValue("participant_id", participantId)
                            .addValue("observation_id", observationId),
                    getRowMapper());
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

    /**
     * The requests the storage gateway has collected data for, waiting for the data health check.
     */
    public List<ObservationResyncRequest> listSynced() {
        return template.query(LIST_SYNCED_RESYNC_REQUESTS, getRowMapper());
    }

    /**
     * Deletes the request only if it is still synced, so a request renewed in the meantime is kept.
     */
    public void deleteSynced(long studyId, int participantId, int observationId) {
        template.update(DELETE_SYNCED_RESYNC_REQUEST, studyId, participantId, observationId);
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
