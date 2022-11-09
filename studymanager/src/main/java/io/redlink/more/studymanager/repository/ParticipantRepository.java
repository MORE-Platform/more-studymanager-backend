package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.model.Participant;
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
public class ParticipantRepository {

    private static final String INSERT_PARTICIPANT = "INSERT INTO participants(study_id,participant_id,alias,study_group_id) VALUES (:study_id,(SELECT COALESCE(MAX(participant_id),0)+1 FROM participants WHERE study_id = :study_id),:alias,:study_group_id)";
    private static final String GET_PARTICIPANT_BY_IDS = "SELECT * FROM participants WHERE study_id = ? AND participant_id = ?";
    private static final String LIST_PARTICIPANTS_BY_STUDY = "SELECT * FROM participants WHERE study_id = ?";
    private static final String DELETE_PARTICIPANT = "DELETE FROM participants WHERE study_id=? AND participant_id=?";
    private static final String UPDATE_PARTICIPANT =
            "UPDATE participants SET alias = :alias, study_group_id = :study_group_id, modified = now() WHERE study_id = :study_id AND participant_id = :participant_id";
    private static final String SET_NEW_STATUS = "UPDATE participants SET status='new' WHERE study_id = ? AND participant_id = ?";
    private static final String SET_REGISTERED_STATUS = "UPDATE participants SET status='registered' WHERE study_id = ? AND participant_id = ?";
    private static final String DELETE_ALL = "DELETE FROM participants";
    private final JdbcTemplate template;
    private final NamedParameterJdbcTemplate namedTemplate;

    public ParticipantRepository(JdbcTemplate template) {
        this.template = template;
        this.namedTemplate = new NamedParameterJdbcTemplate(template);
    }

    public Participant insert(Participant participant) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            namedTemplate.update(INSERT_PARTICIPANT, toParams(participant), keyHolder, new String[]{"participant_id"});
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("Study " + participant.getStudyId() + " does not exist");
        }
        return getByIds(participant.getStudyId(), keyHolder.getKey().intValue());
    }

    public Participant getByIds(long studyId, int participantId) {
        try {
            return template.queryForObject(GET_PARTICIPANT_BY_IDS, getParticipantRowMapper(), studyId, participantId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Participant> listParticipants(Long studyId) {
        return template.query(LIST_PARTICIPANTS_BY_STUDY, getParticipantRowMapper(), studyId);
    }

    public void deleteParticipant(Long studyId, Integer participantId) {
        template.update(DELETE_PARTICIPANT, studyId, participantId);
    }

    public Participant update(Participant participant) {
        namedTemplate.update(UPDATE_PARTICIPANT, toParams(participant).addValue("participant_id", participant.getParticipantId()));
        template.update(getStatus(participant.getStatus()), participant.getStudyId(), participant.getParticipantId());
        return getByIds(participant.getStudyId(), participant.getParticipantId());
    }

    private String getStatus(Participant.Status status) {
        return switch (status) {
            case NEW -> SET_NEW_STATUS;
            case REGISTERED -> SET_REGISTERED_STATUS;
        };
    }

    public void clear() {
        template.update(DELETE_ALL);
    }

    private static MapSqlParameterSource toParams(Participant participant) {
        return new MapSqlParameterSource()
                .addValue("study_id", participant.getStudyId())
                .addValue("alias", participant.getAlias())
                .addValue("study_group_id", participant.getStudyGroupId());
    }

    private static RowMapper<Participant> getParticipantRowMapper() {
        return (rs, rowNum) -> new Participant()
                .setStudyId(rs.getLong("study_id"))
                .setParticipantId(rs.getInt("participant_id"))
                .setAlias(rs.getString("alias"))
                .setStudyGroupId(rs.getInt("study_group_id"))
                .setCreated(rs.getTimestamp("created"))
                .setModified(rs.getTimestamp("modified"))
                .setStatus(Participant.Status.valueOf(rs.getString("status").toUpperCase()));
    }
}
