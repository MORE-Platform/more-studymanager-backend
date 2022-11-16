package io.redlink.more.studymanager.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.redlink.more.studymanager.core.properties.ActionProperties;
import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.model.Action;
import io.redlink.more.studymanager.model.Intervention;
import io.redlink.more.studymanager.utils.MapperUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InterventionRepository {

    private static final String INSERT_INTERVENTION = "INSERT INTO interventions(study_id,intervention_id,title,purpose,study_group_id,schedule) VALUES (:study_id,(SELECT COALESCE(MAX(intervention_id),0)+1 FROM interventions WHERE study_id = :study_id),:title,:purpose,:study_group_id,:schedule::jsonb)";
    private static final String GET_INTERVENTION_BY_IDS = "SELECT * FROM interventions WHERE study_id = ? AND intervention_id = ?";
    private static final String LIST_INTERVENTIONS = "SELECT * FROM interventions WHERE study_id = ?";
    private static final String DELETE_INTERVENTION_BY_IDS = "DELETE FROM interventions WHERE study_id = ? AND intervention_id = ?";
    private static final String DELETE_ALL = "DELETE FROM interventions";
    private static final String UPDATE_INTERVENTION = "UPDATE interventions SET title=:title, purpose=:purpose, schedule=:schedule::jsonb WHERE study_id=:study_id AND intervention_id=:intervention_id";
    private static final String CREATE_ACTION = "INSERT INTO actions(study_id,intervention_id,action_id,type,properties) VALUES (:study_id,:intervention_id,(SELECT COALESCE(MAX(action_id),0)+1 FROM actions WHERE study_id = :study_id AND intervention_id=:intervention_id),:type,:properties::jsonb)";
    private static final String GET_ACTION_BY_IDS = "SELECT * FROM actions WHERE study_id=? AND intervention_id=? AND action_id=?";
    private static final String LIST_ACTIONS = "SELECT * FROM actions WHERE study_id = ? AND intervention_id = ?";
    private static final ObjectMapper mapper = new ObjectMapper();
    private final JdbcTemplate template;
    private final NamedParameterJdbcTemplate namedTemplate;

    public InterventionRepository(JdbcTemplate template) {
        this.template = template;
        this.namedTemplate = new NamedParameterJdbcTemplate(template);
    }

    public Intervention insert(Intervention intervention) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            namedTemplate.update(INSERT_INTERVENTION, toParams(intervention), keyHolder, new String[] { "intervention_id" });
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("Study " + intervention.getStudyId() + " does not exist");
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
        namedTemplate.update(UPDATE_INTERVENTION, toParams(intervention).addValue("intervention_id", intervention.getInterventionId()));
        return getByIds(intervention.getStudyId(), intervention.getInterventionId());
    }

    public Action createAction(Long studyId, Integer interventionId, Action action) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            namedTemplate.update(CREATE_ACTION, actionToParams(studyId, interventionId, action), keyHolder, new String[] { "intervention_id" });
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

    public void clear() {
        template.update(DELETE_ALL);
    }

    private static MapSqlParameterSource actionToParams(Long studyId, Integer interventionId, Action action) {
        try {
            return new MapSqlParameterSource()
                    .addValue("study_id", studyId)
                    .addValue("intervention_id", interventionId)
                    .addValue("type", action.getType())
                    .addValue("properties", mapper.writeValueAsString(action.getProperties()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    private static MapSqlParameterSource toParams(Intervention intervention) {
        try {
            return new MapSqlParameterSource()
                    .addValue("study_id", intervention.getStudyId())
                    .addValue("title", intervention.getTitle())
                    .addValue("purpose", intervention.getPurpose())
                    .addValue("study_group_id", intervention.getStudyGroupId())
                    .addValue("schedule", mapper.writeValueAsString(intervention.getSchedule()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static RowMapper<Action> getActionRowMapper() {
        return (rs, rowNum) -> {
            return new Action()
                    .setActionId(rs.getInt("action_id"))
                    .setType(rs.getString("type"))
                    .setProperties(MapperUtils.readValue(rs.getObject("properties"), ActionProperties.class))
                    .setModified(rs.getTimestamp("modified").toInstant())
                    .setCreated(rs.getTimestamp("created").toInstant());
        };
    }

    private static RowMapper<Intervention> getInterventionRowMapper() {
        return (rs, rowNum) -> {
            return new Intervention()
                    .setStudyId(rs.getLong("study_id"))
                    .setInterventionId(rs.getInt("intervention_id"))
                    .setTitle(rs.getString("title"))
                    .setPurpose(rs.getString("purpose"))
                    .setSchedule(MapperUtils.readValue(rs.getString("schedule"), Object.class))
                    .setStudyGroupId(rs.getInt("study_group_id"))
                    .setCreated(rs.getTimestamp("created").toInstant())
                    .setModified(rs.getTimestamp("modified").toInstant());
        };
    }

}
