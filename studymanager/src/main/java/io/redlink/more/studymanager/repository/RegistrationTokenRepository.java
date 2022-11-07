package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.model.RegistrationToken;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class RegistrationTokenRepository {
    private final JdbcTemplate template;
    private final NamedParameterJdbcTemplate namedTemplate;
    private static final String INSERT_REGISTRATION_TOKEN = "INSERT INTO registration_tokens(study_id,participant_id,token) VALUES (:study_id,:participant_id,:token)";
    private static final String GET_TOKEN_BY_IDS = "SELECT * FROM registration_tokens WHERE study_id=? AND participant_id=?";

    public RegistrationTokenRepository(JdbcTemplate template) {
        this.template = template;
        this.namedTemplate = new NamedParameterJdbcTemplate(template);
    }

    public RegistrationToken insert(RegistrationToken registrationToken) {
        try {
            namedTemplate.update(INSERT_REGISTRATION_TOKEN, toParams(registrationToken));
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("Participant " + registrationToken.getParticipantId() + " or study " + registrationToken.getStudyId() + " does not exist");
        }
        return getByIds(registrationToken.getStudyId(), registrationToken.getParticipantId());
    }

    public RegistrationToken getByIds(Long studyId, Integer participantId) {
        try {
            return template.queryForObject(GET_TOKEN_BY_IDS, getRegistrationTokenRowMapper(), studyId, participantId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private static RowMapper<RegistrationToken> getRegistrationTokenRowMapper() {
        return (rs, rowNum) -> new RegistrationToken()
                .setStudyId(rs.getLong("study_id"))
                .setParticipantId(rs.getInt("participant_id"))
                .setToken(rs.getString("token"));
    }

    private static MapSqlParameterSource toParams(RegistrationToken registrationToken) {
        return new MapSqlParameterSource()
                .addValue("study_id", registrationToken.getStudyId())
                .addValue("participant_id", registrationToken.getParticipantId())
                .addValue("token", registrationToken.getToken());
    }

}
