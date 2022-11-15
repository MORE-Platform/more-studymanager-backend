package io.redlink.more.studymanager.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.redlink.more.studymanager.core.properties.TriggerProperties;
import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.model.Intervention;
import io.redlink.more.studymanager.model.Trigger;
import io.redlink.more.studymanager.utils.MapperUtils;
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
    private static final String UPSERT_TRIGGER = "INSERT INTO triggers(study_id,intervention_id,type,properties) VALUES(:study_id,:intervention_id,:type,:properties::jsonb) ON CONFLICT ON CONSTRAINT triggers_pkey DO UPDATE SET type=:type, properties=:properties::jsonb, modified = now()";
    private static final String GET_TRIGGER_BY_IDS = "SELECT * FROM triggers WHERE study_id = ? AND intervention_id = ?";
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
            namedTemplate.update(INSERT_INTERVENTION, interventionToParams(intervention), keyHolder, new String[] { "intervention_id" });
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

    public void clear() {
        template.update(DELETE_ALL);
    }

    private static MapSqlParameterSource interventionToParams(Intervention intervention) {
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

    private static MapSqlParameterSource triggerToParams(Long studyId, Integer interventionId, Trigger trigger) {
        try {
            return new MapSqlParameterSource()
                    .addValue("study_id", studyId)
                    .addValue("intervention_id", interventionId)
                    .addValue("type", trigger.getType())
                    .addValue("properties", mapper.writeValueAsString(trigger.getProperties()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static RowMapper<Trigger> getTriggerRowMapper() {
        return (rs, rowNum) -> new Trigger()
                .setProperties(MapperUtils.readValue(rs.getString("properties"), TriggerProperties.class))
                .setType(rs.getString("type"))
                .setCreated(rs.getTimestamp("created").toInstant())
                .setModified(rs.getTimestamp("modified").toInstant());
    }

    private static RowMapper<Intervention> getInterventionRowMapper() {
        return (rs, rowNum) -> {
            try {
                return new Intervention()
                        .setStudyId(rs.getLong("study_id"))
                        .setInterventionId(rs.getInt("intervention_id"))
                        .setTitle(rs.getString("title"))
                        .setPurpose(rs.getString("purpose"))
                        .setSchedule(mapper.readValue(rs.getString("schedule"), Object.class))
                        .setStudyGroupId(rs.getInt("study_group_id"))
                        .setCreated(rs.getTimestamp("created").toInstant())
                        .setModified(rs.getTimestamp("modified").toInstant());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        };
    }

}
