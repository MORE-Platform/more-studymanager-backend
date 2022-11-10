package io.redlink.more.studymanager.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.model.Observation;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ObservationRepository {

    private final String INSERT_NEW_OBSERVATION = "INSERT INTO observations(study_id,observation_id,title,purpose,participant_info,type,study_group_id,properties,schedule) VALUES (:study_id,(SELECT COALESCE(MAX(observation_id),0)+1 FROM observations WHERE study_id = :study_id),:title,:purpose,:participant_info,:type,:study_group_id,:properties::jsonb,:schedule::jsonb)";
    private final String GET_OBSERVATION_BY_IDS = "SELECT * FROM observations WHERE study_id = ? AND observation_id = ?";
    private final String DELETE_BY_IDS = "DELETE FROM observations WHERE study_id = ? AND observation_id = ?";
    private final String LIST_OBSERVATIONS = "SELECT * FROM observations WHERE study_id = ?";
    private final String UPDATE_OBSERVATION = "UPDATE observations SET title=:title, purpose=:purpose, participant_info=:participant_info, type=:type, study_group_id=:study_group_id, properties=:properties::jsonb, schedule=:schedule::jsonb, modified=now() WHERE study_id=:study_id AND observation_id=:observation_id";
    private final String DELETE_ALL = "DELETE FROM observations";
    private final JdbcTemplate template;
    private final NamedParameterJdbcTemplate namedTemplate;
    private static final ObjectMapper mapper = new ObjectMapper();

    public ObservationRepository(JdbcTemplate template) {
        this.template = template;
        this.namedTemplate = new NamedParameterJdbcTemplate(template);
    }

    public Observation insert(Observation observation) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            namedTemplate.update(INSERT_NEW_OBSERVATION, toParams(observation), keyHolder, new String[] { "observation_id" });
        } catch (DataIntegrityViolationException | JsonProcessingException e) {
            throw new BadRequestException("Study group " + observation.getStudyGroupId() + " does not exist on study " + observation.getStudyId());
        }
        return getByIds(observation.getStudyId(), keyHolder.getKey().intValue());
    }

    private Observation getByIds(Long studyId, Integer observationId) {
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
        return template.query(LIST_OBSERVATIONS, getObservationRowMapper(), studyId);
    }

    public Observation updateObservation(Observation observation) {
        try {
            namedTemplate.update(UPDATE_OBSERVATION,
                    toParams(observation).addValue("observation_id", observation.getObservationId()));
            return getByIds(observation.getStudyId(), observation.getObservationId());
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public void clear() {
        template.execute(DELETE_ALL);
    }

    private static MapSqlParameterSource toParams(Observation observation) throws JsonProcessingException {
        return new MapSqlParameterSource()
                .addValue("study_id", observation.getStudyId())
                .addValue("title", observation.getTitle())
                .addValue("purpose", observation.getPurpose())
                .addValue("participant_info", observation.getParticipantInfo())
                .addValue("type", observation.getType())
                .addValue("study_group_id", observation.getStudyGroupId())
                .addValue("properties", mapper.writeValueAsString(observation.getProperties()))
                .addValue("schedule", mapper.writeValueAsString(observation.getSchedule()));
    }

    private static RowMapper<Observation> getObservationRowMapper() {
        return (rs, rowNum) -> {
            try {
                return new Observation()
                        .setStudyId(rs.getLong("study_id"))
                        .setObservationId(rs.getInt("observation_id"))
                        .setTitle(rs.getString("title"))
                        .setPurpose(rs.getString("purpose"))
                        .setParticipantInfo(rs.getString("participant_info"))
                        .setType(rs.getString("type"))
                        .setStudyGroupId(rs.getInt("study_group_id"))
                        .setProperties(mapper.readValue(rs.getString("properties"), Object.class))
                        .setSchedule(mapper.readValue(rs.getString("schedule"), Object.class))
                        .setCreated(rs.getTimestamp("created"))
                        .setModified(rs.getTimestamp("modified"));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
