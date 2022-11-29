package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.model.User;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class StudyRepository {

    private static final String INSERT_STUDY =
            "INSERT INTO studies (title,purpose,participant_info,consent_info,planned_start_date,planned_end_date) " +
            "VALUES (:title,:purpose,:participant_info,:consent_info,:planned_start_date,:planned_end_date) " +
            "RETURNING *";
    private static final String GET_STUDY_BY_ID =
            "SELECT *, " +
            "  (SELECT user_roles FROM study_roles_by_user WHERE study_roles_by_user.study_id = studies.study_id AND user_id = :userId) AS user_roles " +
            "FROM studies " +
            "WHERE study_id = :studyId";
    private static final String LIST_STUDIES_ORDER_BY_MODIFIED_DESC = "SELECT * FROM studies ORDER BY modified DESC";
    private static final String LIST_STUDIES_BY_STATUS = "SELECT * FROM studies WHERE status = ?::study_state";
    private static final String LIST_STUDY_BY_ACL =
            "SELECT studies.*, acl.user_roles " +
            "FROM studies " +
            "    INNER JOIN (SELECT acl.study_id, array_agg(user_role) AS user_roles FROM study_acl acl WHERE acl.user_id = :userId AND acl.user_role IN (:roles) GROUP BY acl.study_id) acl " +
            "    ON (studies.study_id = acl.study_id) " +
            "ORDER BY modified DESC";
    private static final String UPDATE_STUDY =
            "UPDATE studies SET title = :title, purpose = :purpose, participant_info = :participant_info, consent_info = :consent_info, planned_start_date = :planned_start_date, planned_end_date = :planned_end_date, modified = now() " +
            "WHERE study_id = :study_id " +
            "RETURNING *, (SELECT user_roles FROM study_roles_by_user WHERE study_roles_by_user.study_id = studies.study_id AND user_id = :userId) AS user_roles";

    private static final String DELETE_BY_ID = "DELETE FROM studies WHERE study_id = ?";
    private static final String CLEAR_STUDIES = "DELETE FROM studies";
    private static final String SET_DRAFT_STATE_BY_ID = "UPDATE studies SET status = 'draft', start_date = NULL, end_date = NULL, modified = now() WHERE study_id = ?";
    private static final String SET_ACTIVE_STATE_BY_ID = "UPDATE studies SET status = 'active', start_date = now(), modified = now() WHERE study_id = ?";
    private static final String SET_PAUSED_STATE_BY_ID = "UPDATE studies SET status = 'paused', modified = now() WHERE study_id = ?";
    private static final String SET_CLOSED_STATE_BY_ID = "UPDATE studies SET status = 'closed', end_date = now(), modified = now() WHERE study_id = ?";

    private final JdbcTemplate template;
    private final NamedParameterJdbcTemplate namedTemplate;

    public StudyRepository(JdbcTemplate template) {
        this.template = template;
        this.namedTemplate = new NamedParameterJdbcTemplate(template);
    }

    public Study insert(Study study) {
        return namedTemplate.queryForObject(INSERT_STUDY, studyToParams(study), getStudyRowMapper());
    }

    public Study getById(long id) {
        return getById(id, null);
    }

    public Study getById(long id, User user) {
        return namedTemplate.queryForStream(GET_STUDY_BY_ID,
                        new MapSqlParameterSource()
                                .addValue("studyId", id)
                                .addValue("userId", user != null ? user.id() : null),
                        getStudyRowMapperWithUserRoles())
                .findFirst()
                .orElse(null);
    }

    public List<Study> listStudyOrderByModifiedDesc() {
        return template.query(LIST_STUDIES_ORDER_BY_MODIFIED_DESC, getStudyRowMapper());
    }

    public List<Study> listStudiesByAclOrderByModifiedDesc(User user, Set<StudyRole> allowedRoles) {
        return namedTemplate.query(LIST_STUDY_BY_ACL,
                StudyAclRepository.createParams(user.id(), allowedRoles),
                getStudyRowMapperWithUserRoles());
    }

    public Study update(Study study, User user) {
        return namedTemplate.queryForStream(UPDATE_STUDY,
                        studyToParams(study)
                                .addValue("study_id", study.getStudyId())
                                .addValue("userId", user != null ? user.id() : null),
                        getStudyRowMapperWithUserRoles()
                )
                .findFirst()
                .orElse(null);
    }

    public void deleteById(long id) {
        template.update(DELETE_BY_ID, id);
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
                .addValue("planned_end_date", study.getPlannedEndDate())
                ;
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
                .setStudyState(Study.Status.valueOf(rs.getString("status").toUpperCase()))
                ;
    }

    private static RowMapper<Study> getStudyRowMapperWithUserRoles() {
        return ((rs, rowNum) -> {
            var study = getStudyRowMapper().mapRow(rs, rowNum);
            if (study == null) return null;
            return study.setUserRoles(StudyAclRepository.readRoleArray(rs, "user_roles"));
        });
    }

    // for testing purpose only
    protected void clear() {
        template.execute(CLEAR_STUDIES);
    }

    public List<Study> listStudiesByStatus(Study.Status status) {
        return template.query(LIST_STUDIES_BY_STATUS, getStudyRowMapper(), status.getValue());
    }
}
