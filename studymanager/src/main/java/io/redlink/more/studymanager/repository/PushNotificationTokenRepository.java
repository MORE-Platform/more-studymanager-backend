package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.model.PushNotificationsToken;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class PushNotificationTokenRepository {
    private static final String GET_TOKEN_BY_ID = "SELECT service, token FROM push_notifications_token t WHERE t.study_id = :study_id AND t.participant_id = :participant_id";
    private static final String CLEAR_TOKEN_BY_ID = "DELETE FROM push_notifications_token t WHERE t.study_id = :study_id AND t.participant_id = :participant_id";

    private final NamedParameterJdbcTemplate namedTemplate;

    public PushNotificationTokenRepository(JdbcTemplate template) {
        this.namedTemplate = new NamedParameterJdbcTemplate(template);
    }


    public Optional<PushNotificationsToken> getTokenById(long studyId, int participantId) {
        try (var stream = namedTemplate.queryForStream(GET_TOKEN_BY_ID,
                createParams(studyId, participantId),
                getTokenRowMapper())) {
            return stream.findFirst();
        }
    }

    public boolean deleteToken(long studyID, int participantId) {
        return 1 == namedTemplate.update(CLEAR_TOKEN_BY_ID, createParams(studyID, participantId));
    }

    private static MapSqlParameterSource createParams(long studyId, int participantId) {
        return new MapSqlParameterSource()
                .addValue("study_id", studyId)
                .addValue("participant_id", participantId);
    }

    private static RowMapper<PushNotificationsToken> getTokenRowMapper() {
        return (rs, rowNum) -> new PushNotificationsToken(
                rs.getString("service"),
                rs.getString("token")
        );
    }
}
