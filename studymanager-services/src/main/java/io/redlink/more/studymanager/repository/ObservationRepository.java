/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.model.ParticipantWithObservationProperties;
import io.redlink.more.studymanager.model.scheduler.ScheduleEvent;
import io.redlink.more.studymanager.utils.MapperUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.redlink.more.studymanager.repository.RepositoryUtils.getValidNullableIntegerValue;

@Component
public class ObservationRepository {

    private final static Logger LOG = LoggerFactory.getLogger(ObservationRepository.class);

    private static final String INSERT_NEW_OBSERVATION = "INSERT INTO observations(study_id,observation_id,title,purpose,participant_info,type,study_group_id,properties,schedule,hidden,no_schedule,reminder) VALUES (:study_id,(SELECT COALESCE(MAX(observation_id),0)+1 FROM observations WHERE study_id = :study_id),:title,:purpose,:participant_info,:type,:study_group_id,:properties::jsonb,:schedule::jsonb,:hidden,:no_schedule,:reminder)";
    private static final String IMPORT_OBSERVATION = "INSERT INTO observations(study_id,observation_id,title,purpose,participant_info,type,study_group_id,properties,schedule,hidden,no_schedule,reminder) VALUES (:study_id,:observation_id,:title,:purpose,:participant_info,:type,:study_group_id,:properties::jsonb,:schedule::jsonb,:hidden,:no_schedule,:reminder)";
    private static final String GET_OBSERVATION_BY_IDS = """
            SELECT o.*, ARRAY_AGG(oog.observation_group_id) FILTER (WHERE oog.observation_group_id IS NOT NULL) AS observation_group_ids
            FROM observations o
                LEFT JOIN observation_observation_groups oog ON o.study_id = oog.study_id AND o.observation_id = oog.observation_id
            WHERE o.study_id = ? AND o.observation_id = ?
            GROUP BY o.study_id, o.observation_id""";
    private static final String DELETE_BY_IDS = "DELETE FROM observations WHERE study_id = ? AND observation_id = ?";
    private static final String LIST_OBSERVATIONS = """
            SELECT o.*, ARRAY_AGG(oog.observation_group_id) FILTER (WHERE oog.observation_group_id IS NOT NULL) AS observation_group_ids
            FROM observations o
                LEFT JOIN observation_observation_groups oog ON o.study_id = oog.study_id AND o.observation_id = oog.observation_id
            WHERE o.study_id = :study_id
            GROUP BY o.study_id, o.observation_id""";
    private static final String LIST_OBSERVATIONS_FOR_GROUP = """
            SELECT o.*, ARRAY_AGG(oog.observation_group_id) FILTER (WHERE oog.observation_group_id IS NOT NULL) AS observation_group_ids
            FROM observations o
                LEFT JOIN observation_observation_groups oog ON o.study_id = oog.study_id AND o.observation_id = oog.observation_id
            WHERE o.study_id = :study_id
              AND (o.study_group_id IS NULL OR o.study_group_id = :study_group_id)
              AND (NOT EXISTS (
                SELECT 1 FROM observation_observation_groups oog3\s
                WHERE oog3.study_id = o.study_id
                  AND oog3.observation_id = o.observation_id
                ) OR EXISTS (
                  SELECT 1 FROM observation_observation_groups oog2
                  WHERE oog2.study_id = o.study_id
                    AND oog2.observation_id = o.observation_id
                    AND oog2.observation_group_id = ANY(:observation_group_ids)))
            GROUP BY o.study_id, o.observation_id""";
    private static final String UPDATE_OBSERVATION = "UPDATE observations SET title=:title, purpose=:purpose, participant_info=:participant_info, study_group_id=:study_group_id, properties=:properties::jsonb, schedule=:schedule::jsonb, modified=now(), hidden=:hidden, no_schedule=:no_schedule, reminder=:reminder WHERE study_id=:study_id AND observation_id=:observation_id";
    private static final String DELETE_ALL = "DELETE FROM observations";
    private static final String SET_OBSERVATION_PROPERTIES_FOR_PARTICIPANT = "INSERT INTO participant_observation_properties(study_id,participant_id,observation_id,properties) VALUES (:study_id,:participant_id,:observation_id,:properties::jsonb) ON CONFLICT (study_id, participant_id, observation_id) DO UPDATE SET properties = EXCLUDED.properties";
    private static final String GET_OBSERVATION_PROPERTIES_FOR_PARTICIPANT = "SELECT properties FROM participant_observation_properties WHERE  study_id = ? AND participant_id = ? AND observation_id = ?";
    private static final String GET_ALL_OBSERVATION_PROPERTIES_FOR_PARTICIPANT = "SELECT * FROM participant_observation_properties WHERE  study_id = ?";
    private static final String DELETE_OBSERVATION_PROPERTIES_FOR_PARTICIPANT = "DELETE FROM participant_observation_properties WHERE study_id = ? AND participant_id = ? AND observation_id = ?";
    private static final String REMOVE_PARTICIPANT_PROPERTY_KEY = "UPDATE participant_observation_properties SET properties = properties - :key WHERE study_id = :study_id AND observation_id = :observation_id";

    /*
     * SQL Statements for managing participant_observation_groups mapping for participants
     */
    private static final String DELETE_OBSERVATION_OBSERVATION_GROUP_IDS =
            "DELETE FROM observation_observation_groups " +
                    "WHERE study_id = :study_id AND observation_id = :observation_id;";

    private static final String SET_OBSERVATION_OBSERVATION_GROUP_IDS =
            "INSERT INTO observation_observation_groups (study_id, observation_id, observation_group_id) " +
                    "SELECT :study_id, :observation_id, unnest(:observation_group_ids::int[]);";


    private final JdbcTemplate template;
    private final NamedParameterJdbcTemplate namedTemplate;

    public ObservationRepository(JdbcTemplate template) {
        this.template = template;
        this.namedTemplate = new NamedParameterJdbcTemplate(template);
    }

    @Transactional
    public Observation insert(Observation observation) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            namedTemplate.update(INSERT_NEW_OBSERVATION, toParams(observation), keyHolder, new String[]{"observation_id"});
        } catch (DataIntegrityViolationException e) {
            String message;
            if (observation.getStudyGroupId() != null) {
                message = String.format("Study group %s does not exist on study %s",
                        observation.getStudyGroupId(), observation.getStudyId());
            } else {
                message = String.format("Encountered %s while inserting observation", e.getClass().getSimpleName());
                LOG.warn("Unable to insert {}", observation, e);
            }
            throw new BadRequestException(message);
        } catch (JsonProcessingException e) {
            LOG.warn("Unable to insert {}", observation, e);
            throw new BadRequestException("Unable to insert observation (" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
        }
        Integer observationId = keyHolder.getKey().intValue();
        setObservationObservationGroupIds(observation.getStudyId(), observationId, observation.getObservationGroupIds());
        return getById(observation.getStudyId(), observationId);
    }

    @Transactional
    public Observation doImport(Long studyId, Observation observation) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            namedTemplate.update(
                    IMPORT_OBSERVATION,
                    toParams(observation)
                            .addValue("study_id", studyId)
                            .addValue("observation_id", observation.getObservationId()),
                    keyHolder,
                    new String[]{"observation_id"});
        } catch (DataIntegrityViolationException | JsonProcessingException e) {
            throw new BadRequestException(
                    "Error during import of observation " +
                            observation.getObservationId() +
                            "for study " +
                            observation.getStudyId()
            );
        }
        Integer observationId = keyHolder.getKey().intValue();
        setObservationObservationGroupIds(observation.getStudyId(), observationId, observation.getObservationGroupIds());
        return getById(observation.getStudyId(), observationId);
    }

    public Observation getById(Long studyId, Integer observationId) {
        try {
            return template.queryForObject(GET_OBSERVATION_BY_IDS, getObservationRowMapper(), studyId, observationId);
        } catch (EmptyResultDataAccessException e) {
            throw new BadRequestException("Observation " + observationId + " or study " + studyId + " does not exist");
        }
    }

    public void deleteObservation(Long studyId, Integer observationId) {
        template.update(DELETE_BY_IDS, studyId, observationId);
    }

    public List<Observation> listObservations(Long studyId) {
        return namedTemplate.query(
                LIST_OBSERVATIONS,
                new MapSqlParameterSource("study_id", studyId),
                getObservationRowMapper()
        );
    }

    /**
     * Lists all Observation based for the parsed study, study group and as per default no assigned observation group
     *
     * @param studyId      the study
     * @param studyGroupId the study group or NULL of none
     * @return the Observations
     */
    public List<Observation> listObservationsForGroup(Long studyId, Integer studyGroupId) {
        return listObservationsForGroup(studyId, studyGroupId, List.of());
    }

    /**
     * Lists all Observation based for the parsed study, study group and observation groups
     *
     * @param studyId             the study
     * @param studyGroupId        the study group or NULL of none
     * @param observationGroupIds the observation groups or an empty collection if none
     * @return the Observations
     */
    public List<Observation> listObservationsForGroup(Long studyId, Integer studyGroupId, Collection<Integer> observationGroupIds) {
        return namedTemplate.query(
                LIST_OBSERVATIONS_FOR_GROUP,
                new MapSqlParameterSource("study_id", studyId)
                        .addValue("study_group_id", studyGroupId)
                        .addValue("observation_group_ids", observationGroupIds == null ? new Integer[0] : observationGroupIds.toArray(new Integer[0])),
                getObservationRowMapper()
        );
    }

    @Transactional
    public Observation updateObservation(Observation observation) {
        try {
            namedTemplate.update(UPDATE_OBSERVATION,
                    toParams(observation).addValue("observation_id", observation.getObservationId()));
            setObservationObservationGroupIds(observation.getStudyId(), observation.getObservationId(), observation.getObservationGroupIds());
            return getById(observation.getStudyId(), observation.getObservationId());
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public void clear() {
        template.execute(DELETE_ALL);
    }

    public void setParticipantProperties(Long studyId, Integer participantId, Integer observationId, ObservationProperties properties) {
        MapSqlParameterSource data = new MapSqlParameterSource()
                .addValue("study_id", studyId)
                .addValue("participant_id", participantId)
                .addValue("observation_id", observationId)
                .addValue("properties", MapperUtils.writeValueAsString(properties));

        namedTemplate.update(SET_OBSERVATION_PROPERTIES_FOR_PARTICIPANT, data);
    }

    public Optional<ObservationProperties> getParticipantProperties(Long studyId, Integer participantId, Integer observationId) {
        try {
            return Optional.ofNullable(template.queryForObject(
                    GET_OBSERVATION_PROPERTIES_FOR_PARTICIPANT,
                    getParticipantObservationPropertiesRowMapper(),
                    studyId,
                    participantId,
                    observationId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void mergeParticipantProperties(Long studyId, Integer participantId, Integer observationId, ObservationProperties properties) {
        var oldProps = getParticipantProperties(studyId, participantId, observationId).orElse(new ObservationProperties());
        MapSqlParameterSource data = new MapSqlParameterSource()
                .addValue("study_id", studyId)
                .addValue("participant_id", participantId)
                .addValue("observation_id", observationId)
                .addValue("properties", MapperUtils.writeValueAsString(
                        MapperUtils.mergeObjects(oldProps, properties))
                );

        namedTemplate.update(SET_OBSERVATION_PROPERTIES_FOR_PARTICIPANT, data);
    }

    public void removeParticipantsPropertyKey(Long studyId, Integer observationId, String keyToRemove) {
        if (keyToRemove == null || keyToRemove.isBlank()) {
            throw new BadRequestException("keyToRemove must not be null or blank");
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("study_id", studyId)
                .addValue("observation_id", observationId)
                .addValue("key", keyToRemove);

        namedTemplate.update(REMOVE_PARTICIPANT_PROPERTY_KEY, params);
    }

    @Transactional
    public List<ParticipantWithObservationProperties> getParticipantObservationProperties(Long studyId) {
        try {
            return template.query(
                    GET_ALL_OBSERVATION_PROPERTIES_FOR_PARTICIPANT,
                    getParticipantWithObservationPropertiesRowMapper(),
                    studyId);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    private static RowMapper<ParticipantWithObservationProperties> getParticipantWithObservationPropertiesRowMapper() {
        return (rs, rowNum) -> new ParticipantWithObservationProperties(
                rs.getInt("participant_id"),
                rs.getLong("study_id"),
                rs.getInt("observation_id"),
                (Map<String, Object>) MapperUtils.readValue(rs.getString("properties"), Map.class)
        );
    }

    public void removeParticipantProperties(Long studyId, Integer participantId, Integer observationId) {
        template.update(DELETE_OBSERVATION_PROPERTIES_FOR_PARTICIPANT, studyId, participantId, observationId);
    }

    private static MapSqlParameterSource toParams(Long studyId) {
        return new MapSqlParameterSource()
                .addValue("study_id", studyId)
                ;
    }

    private static MapSqlParameterSource toParams(Long studyId, Integer observationId) {
        return toParams(studyId)
                .addValue("observation_id", observationId);
    }

    private static MapSqlParameterSource toParams(Observation observation) throws JsonProcessingException {
        return toParams(observation.getStudyId())
                .addValue("title", observation.getTitle())
                .addValue("purpose", observation.getPurpose())
                .addValue("participant_info", observation.getParticipantInfo())
                .addValue("type", observation.getType())
                .addValue("study_group_id", observation.getStudyGroupId())
                .addValue("properties", MapperUtils.writeValueAsString(observation.getProperties()))
                .addValue("schedule", MapperUtils.writeValueAsString(observation.getSchedule()))
                .addValue("hidden", observation.getHidden())
                .addValue("no_schedule", observation.getNoSchedule())
                .addValue("reminder", observation.getReminder());
    }

    private static RowMapper<ObservationProperties> getParticipantObservationPropertiesRowMapper() {
        return (rs, rowNum) -> MapperUtils.readValue(rs.getString("properties"), ObservationProperties.class);
    }

    private static RowMapper<Observation> getObservationRowMapper() {
        return (rs, rowNum) -> new Observation()
                .setStudyId(rs.getLong("study_id"))
                .setObservationId(rs.getInt("observation_id"))
                .setTitle(rs.getString("title"))
                .setPurpose(rs.getString("purpose"))
                .setParticipantInfo(rs.getString("participant_info"))
                .setType(rs.getString("type"))
                .setStudyGroupId(getValidNullableIntegerValue(rs, "study_group_id"))
                .setProperties(MapperUtils.readValue(rs.getString("properties"), ObservationProperties.class))
                .setSchedule(MapperUtils.readValue(rs.getString("schedule"), ScheduleEvent.class))
                .setCreated(RepositoryUtils.readInstant(rs, "created"))
                .setModified(RepositoryUtils.readInstant(rs, "modified"))
                .setHidden(rs.getBoolean("hidden"))
                .setNoSchedule(rs.getBoolean("no_schedule"))
                .setObservationGroupIds(RepositoryUtils.readSet(rs, "observation_group_ids", Integer.class))
                .setReminder(rs.getBoolean("reminder"));
    }

    private void setObservationObservationGroupIds(Long studyId, Integer observationId, Set<Integer> observationGroupIds) {
        final var params = toParams(studyId, observationId);
        namedTemplate.update(DELETE_OBSERVATION_OBSERVATION_GROUP_IDS, params);
        if (observationGroupIds != null && !observationGroupIds.isEmpty()) {
            params.addValue("observation_group_ids", observationGroupIds.toArray(new Integer[0]));
            namedTemplate.update(SET_OBSERVATION_OBSERVATION_GROUP_IDS, params);
        }
    }


}
