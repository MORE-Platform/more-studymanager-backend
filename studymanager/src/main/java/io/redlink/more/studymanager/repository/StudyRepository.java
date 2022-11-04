package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.model.Study;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
public class StudyRepository {

    private static final String INSERT_STUDY = "INSERT INTO studies (title) VALUES (:title)";
    private static final String GET_STUDY_BY_ID = "SELECT * FROM studies WHERE study_id = ?";
    private static final String LIST_STUDIES_ORDER_BY_MODIFIED_DESC = "SELECT * FROM studies ORDER BY modified DESC";
    private static final String CLEAR_STUDIES = "DELETE FROM studies";
    private final JdbcTemplate template;
    private final NamedParameterJdbcTemplate namedTemplate;

    public StudyRepository(JdbcTemplate template) {
        this.template = template;
        this.namedTemplate = new NamedParameterJdbcTemplate(template);
    }

    public Study insert(Study study) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        SqlParameterSource parameters = new MapSqlParameterSource(Map.of(
                "title", study.getTitle()
        ));
        namedTemplate.update(INSERT_STUDY, parameters, keyHolder, new String[] { "study_id" });
        return getById(keyHolder.getKey().longValue());
    }

    public Study getById(long id) {
        return template.queryForObject(GET_STUDY_BY_ID, getStudyRowMapper(), id);
    }

    public List<Study> listStudyOrderByModifiedDesc() {
        return template.query(LIST_STUDIES_ORDER_BY_MODIFIED_DESC, getStudyRowMapper());
    }

    private static RowMapper<Study> getStudyRowMapper() {
        return (rs, rowNum) -> new Study()
                .setStudyId(rs.getLong("study_id"))
                .setTitle(rs.getString("title"))
                .setStudyState(Study.Status.valueOf(rs.getString("status").toUpperCase()));
    }

    // for testing purpose only
    protected void clear() {
        template.execute(CLEAR_STUDIES);
    }
}
