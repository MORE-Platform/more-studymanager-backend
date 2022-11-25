package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.core.properties.ActionProperties;
import io.redlink.more.studymanager.core.properties.TriggerProperties;
import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.model.Action;
import io.redlink.more.studymanager.model.Intervention;
import io.redlink.more.studymanager.utils.MapperUtils;
import io.redlink.more.studymanager.model.Trigger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;

import static io.redlink.more.studymanager.utils.RepositoryUtils.getValidNullableIntegerValue;

@Component
public class InterventionRepository {

    private static final String INSERT_INTERVENTION = "INSERT INTO interventions(study_id,intervention_id,title,purpose,study_group_id,schedule) VALUES (:study_id,(SELECT COALESCE(MAX(intervention_id),0)+1 FROM interventions WHERE study_id = :study_id),:title,:purpose,:study_group_id,:schedule::jsonb)";
    private static final String GET_INTERVENTION_BY_IDS = "SELECT * FROM interventions WHERE study_id = ? AND intervention_id = ?";
    private static final String LIST_INTERVENTIONS = "SELECT * FROM interventions WHERE study_id = ?";
    private static final String DELETE_INTERVENTION_BY_IDS = "DELETE FROM interventions WHERE study_id = ? AND intervention_id = ?";
    private static final String DELETE_ALL = "DELETE FROM interventions";
    private static final String UPDATE_INTERVENTION = "UPDATE interventions SET title=:title, study_group_id=:study_group_id, purpose=:purpose, schedule=:schedule::jsonb WHERE study_id=:study_id AND intervention_id=:intervention_id";
    private static final String CREATE_ACTION = "INSERT INTO actions(study_id,intervention_id,action_id,type,properties) VALUES (:study_id,:intervention_id,(SELECT COALESCE(MAX(action_id),0)+1 FROM actions WHERE study_id = :study_id AND intervention_id=:intervention_id),:type,:properties::jsonb)";
    private static final String GET_ACTION_BY_IDS = "SELECT * FROM actions WHERE study_id=? AND intervention_id=? AND action_id=?";
    private static final String LIST_ACTIONS = "SELECT * FROM actions WHERE study_id = ? AND intervention_id = ?";
    private static final String DELETE_ACTION_BY_ID = "DELETE FROM actions WHERE study_id = ? AND intervention_id = ? AND action_id = ?";
    private static final String UPDATE_ACTION = "UPDATE actions SET properties=:properties::jsonb WHERE study_id=:study_id AND intervention_id=:intervention_id AND action_id=:action_id";
    private static final String UPSERT_TRIGGER = "INSERT INTO triggers(study_id,intervention_id,type,properties) VALUES(:study_id,:intervention_id,:type,:properties::jsonb) ON CONFLICT ON CONSTRAINT triggers_pkey DO UPDATE SET type=:type, properties=:properties::jsonb, modified = now()";
    private static final String GET_TRIGGER_BY_IDS = "SELECT * FROM triggers WHERE study_id = ? AND intervention_id = ?";
    private final JdbcTemplate template;
    private final NamedParameterJdbcTemplate namedTemplate;

    public InterventionRepository(JdbcTemplate template) {
        this.template = template;
        this.namedTemplate = new NamedParameterJdbcTemplate(template);
    }

    public Intervention insert(Intervention intervention) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            namedTemplate.update(INSERT_INTERVENTION, interventionToParams(intervention), keyHolder, new String[] { "intervention_id" });
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("Study group " + intervention.getStudyGroupId() + " does not exist on study " + intervention.getStudyId());
        }
        return getByIds(intervention.getStudyId(), keyHolder.getKey().intValue());
    }

    public List<Intervention> listInterventions(Long studyId) {
        return template.query(LIST_INTERVENTIONS, getInterventionRowMapper(), studyId);
    }

    public Intervention getByIds(Long studyId, Integer interventionId) {
        return template.queryForObject(GET_INTERVENTION_BY_IDS, getInterventionRowMapper(), studyId, interventionId);
    }

    public void deleteByIds(Long studyId, Integer interventionId) {
        template.update(DELETE_INTERVENTION_BY_IDS, studyId, interventionId);
    }

    public Intervention updateIntervention(Intervention intervention) {
        namedTemplate.update(UPDATE_INTERVENTION, interventionToParams(intervention).addValue("intervention_id", intervention.getInterventionId()));
        return getByIds(intervention.getStudyId(), intervention.getInterventionId());
    }

    public Trigger updateTrigger(Long studyId, Integer interventionId, Trigger trigger) {
        namedTemplate.update(UPSERT_TRIGGER, triggerToParams(studyId, interventionId, trigger));
        return getTriggerByIds(studyId, interventionId);
    }

    public Trigger getTriggerByIds(Long studyId, Integer interventionId) {
        return template.queryForObject(GET_TRIGGER_BY_IDS, getTriggerRowMapper(), studyId, interventionId);
    }

    public Action createAction(Long studyId, Integer interventionId, Action action) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            namedTemplate.update(CREATE_ACTION, actionToParams(studyId, interventionId, action), keyHolder, new String[] { "action_id" });
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("Intervention " + interventionId + " does not exist on study " + studyId);
        }
        return getActionByIds(studyId, interventionId, keyHolder.getKey().intValue());
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

    private static MapSqlParameterSource interventionToParams(Intervention intervention) {
        return new MapSqlParameterSource()
                .addValue("study_id", intervention.getStudyId())
                .addValue("title", intervention.getTitle())
                .addValue("purpose", intervention.getPurpose())
                .addValue("study_group_id", intervention.getStudyGroupId())
                .addValue("schedule", MapperUtils.writeValueAsString(intervention.getSchedule()));
    }

    private static MapSqlParameterSource triggerToParams(Long studyId, Integer interventionId, Trigger trigger) {
        return new MapSqlParameterSource()
                .addValue("study_id", studyId)
                .addValue("intervention_id", interventionId)
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
                .setCreated(rs.getTimestamp("created").toInstant())
                .setModified(rs.getTimestamp("modified").toInstant());
    }

    private static RowMapper<Action> getActionRowMapper() {
        return (rs, rowNum) -> new Action()
                .setActionId(rs.getInt("action_id"))
                .setType(rs.getString("type"))
                .setProperties(MapperUtils.readValue(rs.getObject("properties"), ActionProperties.class))
                .setModified(rs.getTimestamp("modified").toInstant())
                .setCreated(rs.getTimestamp("created").toInstant());
    }

    private static RowMapper<Intervention> getInterventionRowMapper() {
        return (rs, rowNum) -> new Intervention()
                .setStudyId(rs.getLong("study_id"))
                .setInterventionId(rs.getInt("intervention_id"))
                .setTitle(rs.getString("title"))
                .setPurpose(rs.getString("purpose"))
                .setSchedule(MapperUtils.readValue(rs.getString("schedule"), Object.class))
                .setStudyGroupId(getValidNullableIntegerValue(rs, "study_group_id"))
                .setCreated(rs.getTimestamp("created").toInstant())
                .setModified(rs.getTimestamp("modified").toInstant());
        }

}
