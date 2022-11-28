package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.model.PushNotificationsToken;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PushNotificationRepository {
    private static final String GET_TOKEN_BY_ID = "SELECT service, token FROM push_notifications_token t WHERE t.study_id = :study_id AND t.participant_id = :participant_id";

    private final JdbcTemplate template;
    private final NamedParameterJdbcTemplate namedTemplate;

    public PushNotificationRepository(JdbcTemplate template) {
        this.template = template;
        this.namedTemplate = new NamedParameterJdbcTemplate(template);
    }


    public Optional<PushNotificationsToken> getTokenById(long studyId, int participantId) {
        return namedTemplate.queryForStream(GET_TOKEN_BY_ID, new MapSqlParameterSource()
                        .addValue("study_id", studyId)
                        .addValue("participant_id", participantId),
                        getTokenRowMapper())
                .findFirst();
    }


    private static RowMapper<PushNotificationsToken> getTokenRowMapper() {
        return (rs, rowNum) -> new PushNotificationsToken(
                rs.getString("service"),
                rs.getString("token")
        );
    }

}
