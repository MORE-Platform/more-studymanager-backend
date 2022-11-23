package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.Study_ACL;
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
public class StudyRepository {

    private static final String INSERT_STUDY = "INSERT INTO studies (title,purpose,participant_info,consent_info,planned_start_date,planned_end_date) VALUES (:title,:purpose,:participant_info,:consent_info,:planned_start_date,:planned_end_date)";
    private static final String GET_STUDY_BY_ID = "SELECT * FROM studies WHERE study_id = ?";
    private static final String LIST_STUDIES_ORDER_BY_MODIFIED_DESC = "SELECT * FROM studies ORDER BY modified DESC";
    private static final String UPDATE_STUDY =
            "UPDATE studies SET title = :title, purpose = :purpose, participant_info = :participant_info, consent_info = :consent_info, planned_start_date = :planned_start_date, planned_end_date = :planned_end_date, modified = now() WHERE study_id = :study_id";

    private static final String DELETE_BY_ID = "DELETE FROM studies WHERE study_id = ?";
    private static final String CLEAR_STUDIES = "DELETE FROM studies";
    private static final String SET_DRAFT_STATE_BY_ID = "UPDATE studies SET status = 'draft', start_date = NULL, end_date = NULL, modified = now() WHERE study_id = ?";
    private static final String SET_ACTIVE_STATE_BY_ID = "UPDATE studies SET status = 'active', start_date = now(), modified = now() WHERE study_id = ?";
    private static final String SET_PAUSED_STATE_BY_ID = "UPDATE studies SET status = 'paused', modified = now() WHERE study_id = ?";
    private static final String SET_CLOSED_STATE_BY_ID = "UPDATE studies SET status = 'closed', end_date = now(), modified = now() WHERE study_id = ?";

    private static final String INSERT_STUDY_ACL = "INSERT INTO study_acl (study_id,user_id,user_role,created,creator_id) VALUES (:study_id,:user_id,:user_role,:created,:creator_id)";
    private static final String DELETE_BY_IDS = "DELETE FROM study_acl WHERE study_id = :study_id AND user_id = :user_id";
    private static final String GET_STUDY_ACL_BY_IDS = "SELECT * FROM study_acl WHERE study_id = :study_id AND user_id = :user_id";
    private static final String UPDATE_STUDY_ACL = "UPDATE study_acl SET user_role = :user_role, created = :created, creator_id = :creator_id WHERE study_id = :study_id AND user_id = :user_id";
    private static final String SET_USER_ROLE_BY_ID = "UPDATE study_acl SET user_role = :user_role WHERE study_id = :study_id AND user_id = :user_id";


    private final JdbcTemplate template;
    private final NamedParameterJdbcTemplate namedTemplate;

    public StudyRepository(JdbcTemplate template) {
        this.template = template;
        this.namedTemplate = new NamedParameterJdbcTemplate(template);
    }

    public Study insert(Study study) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        namedTemplate.update(INSERT_STUDY, studyToParams(study), keyHolder, new String[] { "study_id" });
        return getById(keyHolder.getKey().longValue());
    }

    public Study_ACL insert(Study_ACL study_acl){
        namedTemplate.update(INSERT_STUDY_ACL, studyAclToParams(study_acl));
        return getByIds(study_acl.study_id(), study_acl.user_id());
    }

    public Study getById(long id) {
        try {
            return template.queryForObject(GET_STUDY_BY_ID, getStudyRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Study_ACL getByIds(long study_id, String user_id){
        try {
            return template.queryForObject(GET_STUDY_ACL_BY_IDS, getStudyAclRowMapper(), study_id, user_id);
        } catch (EmptyResultDataAccessException e){
         return null;
        }
    }

    public List<Study> listStudyOrderByModifiedDesc() {
        return template.query(LIST_STUDIES_ORDER_BY_MODIFIED_DESC, getStudyRowMapper());
    }

    public Study update(Study study) {
        namedTemplate.update(UPDATE_STUDY, studyToParams(study).addValue("study_id", study.getStudyId()));
        return getById(study.getStudyId());
    }

    public void deleteById(long id) {
        template.update(DELETE_BY_ID, id);
    }

    public void deleteByIds(long study_id, String user_id){
        template.update(DELETE_BY_IDS, study_id, user_id);
    }

    public void setStateById(long id, Study.Status status) {
        template.update(getStatusQuery(status), id);
    }

    private String getStatusQuery(Study.Status status) {
        return switch (status) {
            case DRAFT -> SET_DRAFT_STATE_BY_ID;
            case ACTIVE -> SET_ACTIVE_STATE_BY_ID;
            case PAUSED -> SET_PAUSED_STATE_BY_ID;
            case CLOSED -> SET_CLOSED_STATE_BY_ID;
        };
    }

    private static MapSqlParameterSource studyToParams(Study study) {
        return new MapSqlParameterSource()
                .addValue("title", study.getTitle())
                .addValue("purpose", study.getPurpose())
                .addValue("participant_info", study.getParticipantInfo())
                .addValue("consent_info", study.getConsentInfo())
                .addValue("planned_start_date", study.getPlannedStartDate())
                .addValue("planned_end_date", study.getPlannedEndDate()
        );
    }

    private static MapSqlParameterSource studyAclToParams(Study_ACL study_acl) {
        return new MapSqlParameterSource()
                .addValue("study_id", study_acl.study_id())
                .addValue("user_id", study_acl.user_id())
                .addValue("user_role", study_acl.user_role())
                .addValue("created", study_acl.created())
                .addValue("creator_id", study_acl.creator_id());
    }

    private static RowMapper<Study> getStudyRowMapper() {
        return (rs, rowNum) -> new Study()
                .setStudyId(rs.getLong("study_id"))
                .setTitle(rs.getString("title"))
                .setPurpose(rs.getString("purpose"))
                .setParticipantInfo(rs.getString("participant_info"))
                .setConsentInfo(rs.getString("consent_info"))
                .setPlannedStartDate(rs.getDate("planned_start_date"))
                .setPlannedEndDate(rs.getDate("planned_end_date"))
                .setStartDate(rs.getDate("start_date"))
                .setEndDate(rs.getDate("end_date"))
                .setCreated(rs.getTimestamp("created"))
                .setModified(rs.getTimestamp("modified"))
                .setStudyState(Study.Status.valueOf(rs.getString("status").toUpperCase()));
    }

    private static RowMapper<Study_ACL> getStudyAclRowMapper() {
        return (rs, rowNum) -> new Study_ACL(
                rs.getLong("study_id"),
                rs.getString("user_id"),
                rs.getString("user_role"),
                rs.getTimestamp("created"),
                rs.getString("creator_id")
        );
    }

    // for testing purpose only
    protected void clear() {
        template.execute(CLEAR_STUDIES);
    }
}
