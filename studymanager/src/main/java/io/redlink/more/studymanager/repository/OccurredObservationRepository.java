/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.core.properties.OccurredObservationProperties;
import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.model.OccurredObservation;
import io.redlink.more.studymanager.utils.MapperUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Types;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.stream.Stream;

@Component
public class OccurredObservationRepository {

    private static final String UPSERT = """
            INSERT INTO occurred_observation(study_id,observation_id,participant_id,start,"end",data_valid,data_state,properties)
            VALUES (:study_id,:observation_id,:participant_id,:start,:end,:data_valid,:data_state::observation_data_state,:properties::jsonb)
            ON CONFLICT (study_id,observation_id,participant_id,start) DO UPDATE SET "end" = :end
            RETURNING *
            """;

    private static final String UPDATE = """
            UPDATE occurred_observation
            SET "end" = :end, data_valid = :data_valid, data_state = :data_state::observation_data_state, properties = :properties::jsonb, modified = now()
            WHERE study_id = :study_id AND participant_id = :participant_id AND observation_id = :observation_id AND start = :start;
            """;

    private static final String GET_BY_PK = """
            SELECT * FROM occurred_observation oo
                     WHERE oo.study_id = :study_id AND oo.participant_id = :participant_id AND oo.observation_id = :observation_id AND oo.start = :start
            """;
    private static final String LIST_OCCURRED_OBSERVATION = """
            SELECT * FROM occurred_observation oo
                    WHERE oo.study_id = :study_id
                        AND (:participant_id::INT IS NULL OR oo.participant_id = :participant_id)
                        AND (:observation_id::INT IS NULL OR oo.observation_id = :observation_id)
                        AND (:data_valid::BOOLEAN IS NULL OR oo.data_valid = :data_valid)
                        AND (:data_states::observation_data_state[] IS NULL OR oo.data_state = ANY(:data_states::observation_data_state[]))
            """;

    private static final String FIND_LAST_START_TIME = """
            SELECT start FROM occurred_observation oo
                    WHERE oo.study_id = :study_id
                        AND (:participant_id::INT IS NULL OR oo.participant_id = :participant_id)
                        AND (:observation_id::INT IS NULL OR oo.observation_id = :observation_id)
                        AND (:data_valid::BOOLEAN IS NULL OR oo.data_valid = :data_valid)
                        AND (:data_states::observation_data_state[] IS NULL OR oo.data_state = ANY(:data_states::observation_data_state[]))
                    ORDER BY oo.start DESC
                    LIMIT 1
            """;

    private static final String DELETE_ALL = "DELETE FROM occurred_observation";
    private static final String CLEANUP_BY_STUDY_ID ="DELETE FROM occurred_observation WHERE study_id = :study_id" ;
    private final JdbcTemplate template;
    private final NamedParameterJdbcTemplate namedTemplate;

    public OccurredObservationRepository(JdbcTemplate template) {
        this.template = template;
        this.namedTemplate = new NamedParameterJdbcTemplate(template);
    }

    /**
     * Creates or Updates the <code>OccurredObservation</code> based on the PK (studyId, observationId, participantid, start)
     * In case of a conflict the <code>end</code> date is updated. All other properties are ignored as the assumption is
     * that other methods are used to update the state of observation occurrencies and this method is only used to create/update
     * occurrences. Changing the <code>end</code> timestamp may happen if a study was paused and reconfigured to change the
     * duration of an observation that already started.
     * @param occurredObservation
     * @return
     */
    @Transactional
    public OccurredObservation upsert(OccurredObservation occurredObservation) {
        try {
            return namedTemplate.queryForObject(UPSERT, toParams(occurredObservation), getOccurredObservationRowMapper());
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException(String.format(
                    "Unable to create participant observation for study '%s', observation '%s' and participant '%s'. " +
                            "One or several of the referenced ids seams not to exist.",
                    occurredObservation.studyId(),
                    occurredObservation.observationId(),
                    occurredObservation.participantId()));
        }
    }

    public OccurredObservation getByIds(long studyId, int participantId, int observationId, Instant start) {
        try {
            return namedTemplate.queryForObject(
                    GET_BY_PK,
                    new MapSqlParameterSource("study_id", studyId)
                            .addValue("participant_id", participantId)
                            .addValue("observation_id", observationId)
                            .addValue("start", OffsetDateTime.ofInstant(start, ZoneOffset.UTC), Types.TIMESTAMP_WITH_TIMEZONE)
                    ,
                    getOccurredObservationRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public OccurredObservation update(OccurredObservation occurredObservation) {
        int changedRows = namedTemplate.update(UPDATE, toParams(occurredObservation));
        return changedRows > 0 ? getByIds(occurredObservation) : null;
    }

    public Stream<OccurredObservation> listOccurredObservations(
            Long studyId, Integer participantId, Integer observationId,
            Boolean dataValid,
            Set<OccurredObservation.DataState> dataStates
    ) {
        return namedTemplate.queryForStream(LIST_OCCURRED_OBSERVATION,
                new MapSqlParameterSource("study_id", studyId)
                        .addValue("participant_id", participantId)
                        .addValue("observation_id", observationId)
                        .addValue("data_valid", dataValid)
                        .addValue("data_states", dataStates == null ? null : dataStates.stream().map(OccurredObservation.DataState::getValue).toArray(String[]::new)),
                getOccurredObservationRowMapper());
    }

    public Instant getLatestStartTime(
            Long studyId, Integer participantId, Integer observationId,
            Boolean dataValid, Set<OccurredObservation.DataState> dataStates
    ) {
        try {
            return namedTemplate.queryForObject(FIND_LAST_START_TIME,
                    new MapSqlParameterSource("study_id", studyId)
                            .addValue("participant_id", participantId)
                            .addValue("observation_id", observationId)
                            .addValue("data_valid", dataValid)
                            .addValue("data_states", dataStates == null ? null : dataStates.stream().map(OccurredObservation.DataState::getValue).toArray(String[]::new)),
                    getInstantRowMapper("start", true));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void clear() {
        template.update(DELETE_ALL);
    }

    private OccurredObservation getByIds(OccurredObservation occurredObservation) {
        return getByIds(
                occurredObservation.studyId(),
                occurredObservation.participantId(),
                occurredObservation.observationId(),
                occurredObservation.start());
    }

    private static MapSqlParameterSource toParams(Long studyId) {
        return new MapSqlParameterSource()
                .addValue("study_id", studyId)
                ;
    }

    private static MapSqlParameterSource toParams(Long studyId, Integer participantId, Integer observationId) {
        return toParams(studyId)
                .addValue("participant_id", participantId)
                .addValue("observation_id", observationId);
    }

    private static MapSqlParameterSource toParams(OccurredObservation partObs) {
        return toParams(partObs.studyId(), partObs.participantId(), partObs.observationId())
                .addValue("start", OffsetDateTime.ofInstant(partObs.start(), ZoneOffset.UTC), Types.TIMESTAMP_WITH_TIMEZONE)
                .addValue("end", OffsetDateTime.ofInstant(partObs.end(), ZoneOffset.UTC), Types.TIMESTAMP_WITH_TIMEZONE)
                .addValue("data_valid", partObs.dataValid())
                .addValue("data_state", partObs.dataState() == null ? null : partObs.dataState().getValue())
                .addValue("properties", MapperUtils.writeValueAsString(partObs.properties()));
    }

    private static RowMapper<OccurredObservation> getOccurredObservationRowMapper() {
        return (rs, rowNum) -> new OccurredObservation(
                rs.getLong("study_id"),
                rs.getInt("observation_id"),
                rs.getInt("participant_id"),
                RepositoryUtils.readInstantUTC(rs, "start"),
                RepositoryUtils.readInstantUTC(rs, "end"),
                rs.getBoolean("data_valid"),
                OccurredObservation.DataState.fromValue(rs.getString("data_state")),
                MapperUtils.readValue(rs.getString("properties"), OccurredObservationProperties.class),
                RepositoryUtils.readInstant(rs, "created"),
                RepositoryUtils.readInstant(rs, "modified")
        );
    }
    private static RowMapper<Instant> getInstantRowMapper(String field, boolean withTimezone) {
        return (rs, rowNum) -> withTimezone ? RepositoryUtils.readInstantUTC(rs, field) :
                RepositoryUtils.readInstant(rs, field);
    }

    /**
     * Cleans all OccurredOccurrencies for the parsed StudyId
     * @param studyId
     */
    public void cleanup(Long studyId) {
        final var params = toParams(studyId);
        namedTemplate.update(CLEANUP_BY_STUDY_ID, params);
    }
}
