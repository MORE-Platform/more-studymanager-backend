/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.model.Notification;
import io.redlink.more.studymanager.utils.MapperUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class NotificationRepository {

    private static final String INSERT_NOTIFICATION = "INSERT INTO notifications (study_id, participant_id, msg_id, type, data) " +
            "VALUES (:study_id, :participant_id, :msg_id, :type::notifications_type, :data)";
    private static final String LIST_ALL = "SELECT * FROM notifications";
    private final JdbcTemplate template;
    private final NamedParameterJdbcTemplate namedTemplate;

    public NotificationRepository(JdbcTemplate template, NamedParameterJdbcTemplate namedTemplate) {
        this.template = template;
        this.namedTemplate = namedTemplate;
    }

    public void insert(Notification notification) {
        this.namedTemplate.update(INSERT_NOTIFICATION, Map.of(
                "study_id", notification.getStudyId(),
                "participant_id", notification.getParticipantId(),
                "msg_id", notification.getMsgId(),
                "type", notification.getType().name().toLowerCase(),
                "data", MapperUtils.writeValueAsString(notification.getData())
        ));
    }

    //just for testing
    protected List<Notification> listAll() {
        return this.template.query(LIST_ALL,getRowMapper());
    }

    private static RowMapper<Notification> getRowMapper() {
        return (rs, rowNum) -> new Notification()
                .setMsgId(rs.getString("msg_id"))
                .setType(Notification.Type.valueOf(rs.getString("type").toUpperCase()))
                .setData(MapperUtils.readValue(rs.getString("data"), Map.class))
                .setStudyId(rs.getLong("study_id"))
                .setParticipantId(rs.getInt("participant_id"));
    }
}
