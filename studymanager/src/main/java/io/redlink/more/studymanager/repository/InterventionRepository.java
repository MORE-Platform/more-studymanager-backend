package io.redlink.more.studymanager.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.model.Intervention;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

@Component
public class InterventionRepository {

    private static final String INSERT_INTERVENTION = "INSERT INTO interventions(study_id,intervention_id,title,purpose,study_group_id,schedule) VALUES (:study_id,(SELECT COALESCE(MAX(intervention_id),0)+1 FROM interventions WHERE study_id = :study_id),:title,:purpose,:study_group_id,:schedule::jsonb)";
    private static final String GET_INTERVENTION_BY_ID = "SELECT * FROM interventions WHERE study_id = ? AND intervention_id = ?";
    private static ObjectMapper mapper = new ObjectMapper();
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

    private Intervention getByIds(Long studyId, Integer interventionId) {
        return template.queryForObject(GET_INTERVENTION_BY_ID, getInterventionRowMapper(), studyId, interventionId);
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

    private static RowMapper<Intervention> getInterventionRowMapper() {
        return (rs, rowNum) -> {
            try {
                return new Intervention()
                        .setStudyId(rs.getLong("study_id"))
                        .setInterventionId(rs.getInt("participant_id"))
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
