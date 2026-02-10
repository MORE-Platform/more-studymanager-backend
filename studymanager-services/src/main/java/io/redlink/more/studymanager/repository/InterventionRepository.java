/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.core.properties.ActionProperties;
import io.redlink.more.studymanager.core.properties.TriggerProperties;
import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.model.Action;
import io.redlink.more.studymanager.model.scheduler.Event;
import io.redlink.more.studymanager.model.Intervention;
import io.redlink.more.studymanager.model.Trigger;
import io.redlink.more.studymanager.utils.MapperUtils;
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
import java.util.Set;

import static io.redlink.more.studymanager.repository.RepositoryUtils.getValidNullableIntegerValue;

@Component
public class InterventionRepository {

    private static final String INSERT_INTERVENTION = "INSERT INTO interventions(study_id,intervention_id,title,purpose,study_group_id,schedule) VALUES (:study_id,(SELECT COALESCE(MAX(intervention_id),0)+1 FROM interventions WHERE study_id = :study_id),:title,:purpose,:study_group_id,:schedule::jsonb)";
    private static final String IMPORT_INTERVENTION = "INSERT INTO interventions(study_id,intervention_id,title,purpose,study_group_id,schedule) VALUES (:study_id,:intervention_id,:title,:purpose,:study_group_id,:schedule::jsonb)";
    private static final String GET_INTERVENTION_BY_IDS = """
        SELECT i.*, ARRAY_AGG(iog.observation_group_id) FILTER (WHERE iog.observation_group_id IS NOT NULL) AS observation_group_ids
        FROM interventions i
            LEFT JOIN intervention_observation_groups iog ON i.study_id = iog.study_id AND i.intervention_id = iog.intervention_id
        WHERE i.study_id = ? AND i.intervention_id = ?
        GROUP BY i.study_id, i.intervention_id""";
    private static final String LIST_INTERVENTIONS = """
        SELECT i.*, ARRAY_AGG(iog.observation_group_id) FILTER (WHERE iog.observation_group_id IS NOT NULL) AS observation_group_ids
        FROM interventions i
            LEFT JOIN intervention_observation_groups iog ON i.study_id = iog.study_id AND i.intervention_id = iog.intervention_id
        WHERE i.study_id = ?
        GROUP BY i.study_id, i.intervention_id""";
    private static final String LIST_INTERVENTIONS_FOR_GROUP = """
        SELECT i.*, ARRAY_AGG(iog.observation_group_id) FILTER (WHERE iog.observation_group_id IS NOT NULL) AS observation_group_ids
        FROM interventions i
            LEFT JOIN intervention_observation_groups iog ON i.study_id = iog.study_id AND i.intervention_id = iog.intervention_id
        WHERE i.study_id = :study_id
          AND (i.study_group_id IS NULL OR i.study_group_id = :study_group_id)
          AND (NOT EXISTS (
            SELECT 1 FROM intervention_observation_groups iog3
            WHERE iog3.study_id = i.study_id
              AND iog3.intervention_id = i.intervention_id
            ) OR EXISTS (
            SELECT 1 FROM intervention_observation_groups iog2
            WHERE iog2.study_id = i.study_id
              AND iog2.intervention_id = i.intervention_id
              AND iog2.observation_group_id = ANY(:observation_group_ids)))
        GROUP BY i.study_id, i.intervention_id""";
    private static final String DELETE_INTERVENTION_BY_IDS = "DELETE FROM interventions WHERE study_id = ? AND intervention_id = ?";
    private static final String DELETE_ALL = "DELETE FROM interventions";
    private static final String UPDATE_INTERVENTION = "UPDATE interventions SET title=:title, study_group_id=:study_group_id, purpose=:purpose, schedule=:schedule::jsonb WHERE study_id=:study_id AND intervention_id=:intervention_id";
    private static final String CREATE_ACTION = "INSERT INTO actions(study_id,intervention_id,action_id,type,properties) VALUES (:study_id,:intervention_id,(SELECT COALESCE(MAX(action_id),0)+1 FROM actions WHERE study_id = :study_id AND intervention_id=:intervention_id),:type,:properties::jsonb) RETURNING *";
    private static final String IMPORT_ACTION = "INSERT INTO actions(study_id,intervention_id,action_id,type,properties) VALUES (:study_id,:intervention_id,:action_id,:type,:properties::jsonb) RETURNING *";
    private static final String GET_ACTION_BY_IDS = "SELECT * FROM actions WHERE study_id=? AND intervention_id=? AND action_id=?";
    private static final String LIST_ACTIONS = "SELECT * FROM actions WHERE study_id = ? AND intervention_id = ?";
    private static final String DELETE_ACTION_BY_ID = "DELETE FROM actions WHERE study_id = ? AND intervention_id = ? AND action_id = ?";
    private static final String UPDATE_ACTION = "UPDATE actions SET properties=:properties::jsonb WHERE study_id=:study_id AND intervention_id=:intervention_id AND action_id=:action_id";
    private static final String UPSERT_TRIGGER = "INSERT INTO triggers(study_id,intervention_id,type,properties) VALUES(:study_id,:intervention_id,:type,:properties::jsonb) ON CONFLICT ON CONSTRAINT triggers_pkey DO UPDATE SET type=:type, properties=:properties::jsonb, modified = now()";
    private static final String IMPORT_TRIGGER = "INSERT INTO triggers(study_id,intervention_id,type,properties) VALUES(:study_id,:intervention_id,:type,:properties::jsonb) RETURNING *";
    private static final String GET_TRIGGER_BY_IDS = "SELECT * FROM triggers WHERE study_id = ? AND intervention_id = ?";

    /*
     * SQL Statements for managing participant_observation_groups mapping for participants
     */
    private static final String DELETE_INVERVENTION_OBSERVATION_GROUP_IDS =
            "DELETE FROM intervention_observation_groups " +
                    "WHERE study_id = :study_id AND intervention_id = :intervention_id;";

    private static final String SET_INVERVENTION_OBSERVATION_GROUP_IDS =
            "INSERT INTO intervention_observation_groups (study_id, intervention_id, observation_group_id) " +
                    "SELECT :study_id, :intervention_id, unnest(:observation_group_ids::int[]);";

    private final JdbcTemplate template;
    private final NamedParameterJdbcTemplate namedTemplate;

    public InterventionRepository(JdbcTemplate template) {
        this.template = template;
        this.namedTemplate = new NamedParameterJdbcTemplate(template);
    }

    public Intervention insert(Intervention intervention) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            namedTemplate.update(INSERT_INTERVENTION, interventionToParams(intervention), keyHolder, new String[]{"intervention_id"});
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException(
                    "Unable to insert Invervention because it refers an study group " + intervention.getStudyGroupId() +
                    "that does not exist for study " + intervention.getStudyId());
        }
        Integer interventionId = keyHolder.getKey().intValue();
        setInverventionObservationGroupIds(intervention.getStudyId(), interventionId, intervention.getObservationGroupIds());
        return getByIds(intervention.getStudyId(), interventionId);
    }

    public Intervention importIntervention(Long studyId, Intervention intervention) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            namedTemplate.update(IMPORT_INTERVENTION,
                    interventionToParams(intervention)
                            .addValue("study_id", studyId)
                            .addValue("intervention_id", intervention.getInterventionId()),
                    keyHolder,
                    new String[]{"intervention_id"});
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException(
                    "Error during import of intervention " + intervention.getInterventionId() + "for study " +
                    intervention.getStudyId() + "(" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
        }
        Integer interventionId = keyHolder.getKey().intValue();
        setInverventionObservationGroupIds(intervention.getStudyId(), interventionId, intervention.getObservationGroupIds());
        return getByIds(intervention.getStudyId(), interventionId);
    }

    public List<Intervention> listInterventions(Long studyId) {
        return template.query(LIST_INTERVENTIONS, getInterventionRowMapper(), studyId);
    }

    public List<Intervention> listInterventionsForGroup(Long studyId, Integer groupId){
        return listInterventionsForGroup(studyId, groupId, Collections.emptyList());
    }
    public List<Intervention> listInterventionsForGroup(Long studyId, Integer groupId, Collection<Integer> observationGroupIds) {
        return namedTemplate.query(LIST_INTERVENTIONS_FOR_GROUP,
                new MapSqlParameterSource("study_id", studyId)
                        .addValue("study_group_id", groupId)
                        .addValue("observation_group_ids", observationGroupIds == null ? new Integer[0] : observationGroupIds.toArray(new Integer[0])),
                getInterventionRowMapper()
        );
    }

    public Intervention getByIds(Long studyId, Integer interventionId) {
        return template.queryForObject(GET_INTERVENTION_BY_IDS, getInterventionRowMapper(), studyId, interventionId);
    }

    public void deleteByIds(Long studyId, Integer interventionId) {
        template.update(DELETE_INTERVENTION_BY_IDS, studyId, interventionId);
    }

    @Transactional
    public Intervention updateIntervention(Intervention intervention) {
        namedTemplate.update(UPDATE_INTERVENTION, interventionToParams(intervention).addValue("intervention_id", intervention.getInterventionId()));
        setInverventionObservationGroupIds(intervention.getStudyId(), intervention.getInterventionId(), intervention.getObservationGroupIds());
        return getByIds(intervention.getStudyId(), intervention.getInterventionId());
    }

    public Trigger updateTrigger(Long studyId, Integer interventionId, Trigger trigger) {
        namedTemplate.update(UPSERT_TRIGGER, triggerToParams(studyId, interventionId, trigger));
        return getTriggerByIds(studyId, interventionId);
    }

    public Trigger importTrigger(Long studyId, Integer interventionId, Trigger trigger) {
        try {
            return namedTemplate.queryForObject(
                    IMPORT_TRIGGER,
                    triggerToParams(studyId, interventionId, trigger),
                    getTriggerRowMapper()
            );
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException(
                    "Error during import of trigger for intervention " +
                            interventionId +
                            "for study " +
                            studyId
            );
        }
    }

    public Trigger getTriggerByIds(Long studyId, Integer interventionId) {
        try {
            return template.queryForObject(GET_TRIGGER_BY_IDS, getTriggerRowMapper(), studyId, interventionId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Action createAction(Long studyId, Integer interventionId, Action action) {
        try {
            return namedTemplate.queryForObject(CREATE_ACTION, actionToParams(studyId, interventionId, action), getActionRowMapper());
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("Intervention " + interventionId + " does not exist on study " + studyId);
        }
    }

    public Action importAction(Long studyId, Integer interventionId, Action action) {
        try {
            return namedTemplate.queryForObject(
                    IMPORT_ACTION,
                    actionToParams(studyId, interventionId, action)
                            .addValue("action_id", action.getActionId()),
                    getActionRowMapper()
            );
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException(
                    "Error during import of action for intervention " +
                            interventionId +
                            "for study " +
                            studyId
            );
        }
    }

    public Action getActionByIds(Long studyId, Integer interventionId, Integer actionId) {
        return template.queryForObject(GET_ACTION_BY_IDS, getActionRowMapper(), studyId, interventionId, actionId);
    }

    public List<Action> listActions(Long studyId, Integer interventionId) {
        return template.query(LIST_ACTIONS, getActionRowMapper(), studyId, interventionId);
    }

    public void deleteActionByIds(Long studyId, Integer interventionId, Integer actionId) {
        template.update(DELETE_ACTION_BY_ID, studyId, interventionId, actionId);
    }

    public Action updateAction(Long studyId, Integer interventionId, Integer actionId, Action action) {
        namedTemplate.update(UPDATE_ACTION, actionToParams(studyId, interventionId, action)
                .addValue("action_id", actionId));
        return getActionByIds(studyId, interventionId, actionId);
    }

    public void clear() {
        template.update(DELETE_ALL);
    }

    private static MapSqlParameterSource toParams(Long studyId) {
        return new MapSqlParameterSource()
                .addValue("study_id", studyId)
                ;
    }

    private static MapSqlParameterSource toParams(Long studyId, Integer interventionId) {
        return toParams(studyId)
                .addValue("intervention_id", interventionId);
    }



    private static MapSqlParameterSource interventionToParams(Intervention intervention) {
        return toParams(intervention.getStudyId())
                .addValue("title", intervention.getTitle())
                .addValue("purpose", intervention.getPurpose())
                .addValue("study_group_id", intervention.getStudyGroupId())
                .addValue("schedule", MapperUtils.writeValueAsString(intervention.getSchedule()));
    }

    private static MapSqlParameterSource triggerToParams(Long studyId, Integer interventionId, Trigger trigger) {
        return toParams(studyId, interventionId)
                .addValue("type", trigger.getType())
                .addValue("properties", MapperUtils.writeValueAsString(trigger.getProperties()));
    }

    private static MapSqlParameterSource actionToParams(Long studyId, Integer interventionId, Action action) {
        return new MapSqlParameterSource()
                .addValue("study_id", studyId)
                .addValue("intervention_id", interventionId)
                .addValue("type", action.getType())
                .addValue("properties", MapperUtils.writeValueAsString(action.getProperties()));
    }

    private static RowMapper<Trigger> getTriggerRowMapper() {
        return (rs, rowNum) -> new Trigger()
                .setProperties(MapperUtils.readValue(rs.getObject("properties").toString(), TriggerProperties.class))
                .setType(rs.getString("type"))
                .setCreated(RepositoryUtils.readInstant(rs,"created"))
                .setModified(RepositoryUtils.readInstant(rs,"modified"));
    }

    private static RowMapper<Action> getActionRowMapper() {
        return (rs, rowNum) -> new Action()
                .setActionId(rs.getInt("action_id"))
                .setType(rs.getString("type"))
                .setProperties(MapperUtils.readValue(rs.getObject("properties"), ActionProperties.class))
                .setModified(RepositoryUtils.readInstant(rs,"modified"))
                .setCreated(RepositoryUtils.readInstant(rs,"created"));
    }

    private static RowMapper<Intervention> getInterventionRowMapper() {
        return (rs, rowNum) -> new Intervention()
                .setStudyId(rs.getLong("study_id"))
                .setInterventionId(rs.getInt("intervention_id"))
                .setTitle(rs.getString("title"))
                .setPurpose(rs.getString("purpose"))
                .setSchedule(MapperUtils.readValue(rs.getString("schedule"), Event.class))
                .setStudyGroupId(getValidNullableIntegerValue(rs, "study_group_id"))
                .setCreated(RepositoryUtils.readInstant(rs,"created"))
                .setModified(RepositoryUtils.readInstant(rs,"modified"))
                .setObservationGroupIds(RepositoryUtils.readSet(rs, "observation_group_ids", Integer.class));
        }

    private void setInverventionObservationGroupIds(Long studyId, Integer interventionId, Set<Integer> observationGroupIds) {
        final var params = toParams(studyId, interventionId);
        namedTemplate.update(DELETE_INVERVENTION_OBSERVATION_GROUP_IDS, params);
        if (observationGroupIds != null && !observationGroupIds.isEmpty()) {
            params.addValue("observation_group_ids", observationGroupIds.toArray(new Integer[0]));
            namedTemplate.update(SET_INVERVENTION_OBSERVATION_GROUP_IDS, params);
        }
    }

}
