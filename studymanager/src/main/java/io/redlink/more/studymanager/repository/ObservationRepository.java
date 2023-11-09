package io.redlink.more.studymanager.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.model.scheduler.Event;
import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.model.scheduler.ScheduleEvent;
import io.redlink.more.studymanager.utils.MapperUtils;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import static io.redlink.more.studymanager.repository.RepositoryUtils.getValidNullableIntegerValue;

@Component
public class ObservationRepository {

    private static final String INSERT_NEW_OBSERVATION = "INSERT INTO observations(study_id,observation_id,title,purpose,participant_info,type,study_group_id,properties,schedule,hidden,no_schedule) VALUES (:study_id,(SELECT COALESCE(MAX(observation_id),0)+1 FROM observations WHERE study_id = :study_id),:title,:purpose,:participant_info,:type,:study_group_id,:properties::jsonb,:schedule::jsonb,:hidden,:no_schedule)";
    private static final String GET_OBSERVATION_BY_IDS = "SELECT * FROM observations WHERE study_id = ? AND observation_id = ?";
    private static final String DELETE_BY_IDS = "DELETE FROM observations WHERE study_id = ? AND observation_id = ?";
    private static final String LIST_OBSERVATIONS = "SELECT * FROM observations WHERE study_id = ?";
    private static final String UPDATE_OBSERVATION = "UPDATE observations SET title=:title, purpose=:purpose, participant_info=:participant_info, study_group_id=:study_group_id, properties=:properties::jsonb, schedule=:schedule::jsonb, modified=now(), hidden=:hidden, no_schedule=:no_schedule WHERE study_id=:study_id AND observation_id=:observation_id";
    private static final String DELETE_ALL = "DELETE FROM observations";
    private static final String SET_OBSERVATION_PROPERTIES_FOR_PARTICIPANT = "INSERT INTO participant_observation_properties(study_id,participant_id,observation_id,properties) VALUES (:study_id,:participant_id,:observation_id,:properties::jsonb) ON CONFLICT (study_id, participant_id, observation_id) DO UPDATE SET properties = EXCLUDED.properties";
    private static final String GET_OBSERVATION_PROPERTIES_FOR_PARTICIPANT = "SELECT properties FROM participant_observation_properties WHERE  study_id = ? AND participant_id = ? AND observation_id = ?";
    private static final String DELETE_OBSERVATION_PROPERTIES_FOR_PARTICIPANT = "DELETE FROM participant_observation_properties WHERE study_id = ? AND participant_id = ? AND observation_id = ?";

    private final JdbcTemplate template;
    private final NamedParameterJdbcTemplate namedTemplate;

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
        return getById(observation.getStudyId(), keyHolder.getKey().intValue());
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
        return template.query(LIST_OBSERVATIONS, getObservationRowMapper(), studyId);
    }

    public Observation updateObservation(Observation observation) {
        try {
            namedTemplate.update(UPDATE_OBSERVATION,
                    toParams(observation).addValue("observation_id", observation.getObservationId()));
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

    public void removeParticipantProperties(Long studyId, Integer participantId, Integer observationId) {
        template.update(DELETE_OBSERVATION_PROPERTIES_FOR_PARTICIPANT, studyId, participantId, observationId);
    }

    private static MapSqlParameterSource toParams(Observation observation) throws JsonProcessingException {
        return new MapSqlParameterSource()
                .addValue("study_id", observation.getStudyId())
                .addValue("title", observation.getTitle())
                .addValue("purpose", observation.getPurpose())
                .addValue("participant_info", observation.getParticipantInfo())
                .addValue("type", observation.getType())
                .addValue("study_group_id", observation.getStudyGroupId())
                .addValue("properties", MapperUtils.writeValueAsString(observation.getProperties()))
                .addValue("schedule", MapperUtils.writeValueAsString(observation.getSchedule()))
                .addValue("hidden", observation.getHidden())
                .addValue("no_schedule", observation.getNoSchedule());
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
                .setNoSchedule(rs.getBoolean("no_schedule"));
    }
}
