package io.redlink.more.studymanager.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.model.audit.AuditLog;
import io.redlink.more.studymanager.utils.MapperUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@Component
public class AuditLogRepository {
    private static final Logger LOG = LoggerFactory.getLogger(AuditLogRepository.class);

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC))
            .setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));


    private static final MapType DETAILS_MAP_TYPE = MapperUtils.MAPPER.getTypeFactory()
            .constructMapType(Map.class, String.class, Object.class);

    private static final String INSERT_AUDIT_LOG =
            "INSERT INTO audit_logs (user_id, study_id, action, state, timestamp, resource, details) " +
            "VALUES (:user_id, :study_id, :action, CAST(:state AS audit_action_state), :timestamp, :resource, :details ) " +
            "RETURNING *";

    private static final String CLEAR_AUDIT_LOG = "DELETE FROM audit_logs";


    private final JdbcTemplate template;
    private final NamedParameterJdbcTemplate namedTemplate;

    public AuditLogRepository(JdbcTemplate template) {
        this.template = template;
        this.namedTemplate = new NamedParameterJdbcTemplate(template);
    }

    public AuditLog insert(AuditLog auditLog) {
        try {
            return namedTemplate.queryForObject(INSERT_AUDIT_LOG, toParams(auditLog), getAuditLogRowMapper());
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("Study " + auditLog.getStudyId() + " does not exist");
        }
    }


    private MapSqlParameterSource toParams(AuditLog auditLog) {
        var params = new MapSqlParameterSource()
                .addValue("id", auditLog.getId())
                .addValue("user_id", auditLog.getUserId())
                .addValue("action", auditLog.getAction())
                .addValue("state", auditLog.getActionState().name())
                .addValue("timestamp", Timestamp.from(auditLog.getTimestamp()))
                .addValue("study_id", auditLog.getStudyId())
                .addValue("resource", auditLog.getResource());

        try {
            params.addValue("details", auditLog.getDetails().isEmpty() ? null :
                            MAPPER.writeValueAsString(auditLog.getDetails()));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("AuditLog details could not be serialized as JSON ", e);
        }
        return params;
    }

    private static RowMapper<AuditLog> getAuditLogRowMapper() {
        return (rs, rowNum) -> {
            final AuditLog auditLog = new AuditLog(
                    rs.getLong("id"),
                    RepositoryUtils.readInstant(rs, "created"),
                    rs.getString("user_id"),
                    rs.getLong("study_id"),
                    rs.getString("action"),
                    RepositoryUtils.readInstant(rs,"timestamp")
            )
                    .setResource(rs.getString("resource"))
                    .setActionState(AuditLog.ActionState.valueOf(rs.getString("state")));
            String detailsStr = rs.getString("details");
            if (detailsStr != null) {
                try {
                    auditLog.setDetails(MAPPER.readValue(detailsStr, DETAILS_MAP_TYPE));
                } catch (JsonProcessingException e) {
                    LOG.warn("Unable to parse 'details' for AuditLog[id: {}] ({}:{})", auditLog.getId(), e.getClass().getSimpleName(), e.getMessage(), e);
                }
            } else {
                auditLog.setDetails(new HashMap<>());
            }
            return  auditLog;
        };
    }

    //for test purposes only
    final void clear() {
        template.execute(CLEAR_AUDIT_LOG);
    }
}
