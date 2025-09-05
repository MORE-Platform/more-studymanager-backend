/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.redlink.more.studymanager.api.v1.model.ActionDTO;
import io.redlink.more.studymanager.api.v1.model.AuditlogDataDTO;
import io.redlink.more.studymanager.api.v1.model.StudyRoleDTO;
import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.model.Auditlog;
import io.redlink.more.studymanager.utils.MapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
public class AuditlogRepository {

    // timestamp is the timestamp when the auditlog is saved to the database -> hence it is set with now()
    @Autowired
    private static final String INSERT_AUDITLOG =
            "WITH a AS ( " +
                    "  INSERT INTO auditlogs( " +
                    "    study_id, auditlog_id, user_id, user_name, user_roles, timestamp, state, action, details " +
                    "  ) VALUES ( " +
                    "    :study_id, " +
                    "    (SELECT COALESCE(MAX(auditlog_id),0)+1 FROM auditlogs WHERE study_id = :study_id), " +
                    "    :user_id, :user_name, :user_roles, now() :state, :action, :details " +
                    "  ) RETURNING auditlog_id, study_id " +
                    ") " +
                    "INSERT INTO auditlog_index(study_id, auditlog_id) " +
                    "SELECT study_id, auditlog_id FROM a";

    private static final String LIST_AUDITLOGS_BY_STUDY_ID =
            "SELECT * FROM auditlogs WHERE study_id = :study_id";

    private static final String GET_AUDITLOG_BY_ID =
            "SELECT * FROM auditlogs WHERE study_id = ? AND auditlog_id = ?";

    private static final String DELETE_BY_STUDY_ID = "DELETE FROM audiologs WHERE study_id = ? AND audiolog_id = ?";

    private static final String DELETE_ALL_BY_STUDY_ID = "DELETE FROM auditlogs WHERE study_id = ?";

    private static final String LIST_ALL = "SELECT * FROM auditlog";


    private final JdbcTemplate template;
    private final NamedParameterJdbcTemplate namedTemplate;


    public AuditlogRepository(JdbcTemplate template, NamedParameterJdbcTemplate namedTemplate) {
        this.template = template;
        this.namedTemplate = new NamedParameterJdbcTemplate(template);
    }

    @Transactional
    public Auditlog insertAuditlog(Auditlog auditlog) {
        try {
            return namedTemplate.queryForObject(INSERT_AUDITLOG, toParams(auditlog), getAuditlogRowMapper());
        } catch (DataIntegrityViolationException | JsonProcessingException e) {
            throw new BadRequestException("Auditlog " + auditlog.getAuditlogId() + " does not exist on study " + auditlog.getStudyId());
        }
    }

    public Auditlog getAuditlogById(long studyId, long auditlogId) {
        try {
            return template.queryForObject(GET_AUDITLOG_BY_ID, getAuditlogRowMapper(), studyId, auditlogId);
        } catch(EmptyResultDataAccessException e) {
            throw new BadRequestException("Auditlog " + auditlogId + " does not exist on study " + studyId);
        }
    }

    public List<Auditlog> listAuditlogsByStudyId(long studyId) {
        return namedTemplate.query(
                LIST_AUDITLOGS_BY_STUDY_ID,
                new MapSqlParameterSource("study_id", studyId),
                getAuditlogRowMapper()
        ) ;
    }

    //just for testing
    protected List<Auditlog> listAll() {
        return this.template.query(LIST_ALL,getAuditlogRowMapper());
    }

    public void deleteAuditlogById(long studyId, long auditlogId) {
        template.update(DELETE_ALL_BY_STUDY_ID, studyId, auditlogId);
    }

    public void deleteAuditlogsByStudyId(long studyId) {
        template.update(DELETE_BY_STUDY_ID, studyId);
    }

    // Helper functions ------
    private static MapSqlParameterSource toParams(Auditlog auditlog) throws JsonProcessingException {
        return new MapSqlParameterSource()
                .addValue("study_id", auditlog.getStudyId())
                .addValue("auditlog_id", auditlog.getAuditlogId())
                .addValue("user_id", auditlog.getUserId())
                .addValue("user_roles", auditlog.getUserRoles())
                .addValue("user_name", auditlog.getUserName())
                .addValue("timestamp", auditlog.getTimestamp())
                .addValue("action", auditlog.getAction())
                .addValue("state", auditlog.getState())
                .addValue("details", auditlog.getAuditlog());
    }

    private static RowMapper<Auditlog> getAuditlogRowMapper() {
        return (rs, rowNum) -> {
            Auditlog auditlog = new Auditlog();

            auditlog.setUserId(rs.getString("user_id"));
            auditlog.setUserName(rs.getString("user_name"));
            auditlog.setTimestamp(rs.getTimestamp("timestamp").toInstant());

            // user roles
            java.sql.Array rolesArray = rs.getArray("user_roles");
            if (rolesArray != null) {
                String[] roles = (String[]) rolesArray.getArray();
                List<StudyRoleDTO> roleList = Arrays.stream(roles)
                        .filter(Objects::nonNull)
                        .map(r -> StudyRoleDTO.valueOf(r.trim().toUpperCase())) // robust gegen DB-Werte
                        .toList(); // seit Java 16, sonst .collect(Collectors.toList())
                auditlog.setUserRoles(roleList);
            }

            // action
            Object actionObj = rs.getObject("action");
            if (actionObj != null) {
                ActionDTO action = new ActionDTO().type(actionObj.toString());
                auditlog.setAction(action);
            }

            // state as enum
            String stateStr = rs.getString("state");
            if (stateStr != null) {
                auditlog.setState(AuditlogDataDTO.StateEnum.valueOf(stateStr));
            }

            // details JSON in map parsen
            String detailsJson = rs.getString("details");
            if (detailsJson != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> details = MapperUtils.readValue(detailsJson, Map.class);
                auditlog.setDetails(details);
            }

            return auditlog;
        };
    }
}
