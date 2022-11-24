package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.model.Study;
import java.util.List;
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
    private static final String GET_STUDY_BY_ID = "SELECT * FROM studies WHERE study_id = ?";
    private static final String LIST_STUDIES_ORDER_BY_MODIFIED_DESC = "SELECT * FROM studies ORDER BY modified DESC";
    private static final String UPDATE_STUDY =
            "UPDATE studies SET title = :title, purpose = :purpose, participant_info = :participant_info, consent_info = :consent_info, planned_start_date = :planned_start_date, planned_end_date = :planned_end_date, modified = now() " +
            "WHERE study_id = :study_id " +
            "RETURNING *";

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
        return template.queryForStream(GET_STUDY_BY_ID, getStudyRowMapper(), id)
                .findFirst()
                .orElse(null);
    }

    public List<Study> listStudyOrderByModifiedDesc() {
        return template.query(LIST_STUDIES_ORDER_BY_MODIFIED_DESC, getStudyRowMapper());
    }

    public Study update(Study study) {
        return namedTemplate.queryForStream(UPDATE_STUDY,
                        studyToParams(study).addValue("study_id", study.getStudyId()),
                        getStudyRowMapper()
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
                .addValue("planned_end_date", study.getPlannedEndDate()
                );
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

    // for testing purpose only
    protected void clear() {
        template.execute(CLEAR_STUDIES);
    }
}
