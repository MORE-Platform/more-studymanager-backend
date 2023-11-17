package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.model.StudyGroup;
import io.redlink.more.studymanager.model.scheduler.Duration;
import io.redlink.more.studymanager.utils.MapperUtils;
import org.springframework.dao.DataIntegrityViolationException;
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
public class StudyGroupRepository {
    private static final String INSERT_STUDY_GROUP = "INSERT INTO study_groups (study_id,study_group_id,title,purpose) VALUES (:study_id,(SELECT COALESCE(MAX(study_group_id),0)+1 FROM study_groups WHERE study_id = :study_id),:title,:purpose)";
    private static final String GET_STUDY_GROUP_BY_IDS = "SELECT * FROM study_groups WHERE study_id = ? AND study_group_id = ?";
    private static final String LIST_STUDY_GROUPS_ORDER_BY_STUDY_GROUP_ID = "SELECT * FROM study_groups WHERE study_id = ? ORDER BY study_group_id";
    private static final String UPDATE_STUDY =
            "UPDATE study_groups SET title = :title, purpose = :purpose, duration = :duration::jsonb, modified = now() WHERE study_id = :study_id AND study_group_id = :study_group_id";

    private static final String DELETE_STUDY_GROUP_BY_ID = "DELETE FROM study_groups WHERE study_id = ? AND study_group_id = ?";
    private static final String CLEAR_STUDY_GROUPS = "DELETE FROM study_groups";

    private final JdbcTemplate template;
    private final NamedParameterJdbcTemplate namedTemplate;

    public StudyGroupRepository(JdbcTemplate template) {
        this.template = template;
        this.namedTemplate = new NamedParameterJdbcTemplate(template);
    }

    public StudyGroup insert(StudyGroup studyGroup) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            namedTemplate.update(INSERT_STUDY_GROUP, toParams(studyGroup), keyHolder, new String[] { "study_group_id" });
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("Study " + studyGroup.getStudyId() + " does not exist");
        }
        return getByIds(studyGroup.getStudyId(), keyHolder.getKey().intValue());
    }

    public StudyGroup getByIds(long studyId, int studyGroupId) {
        try {
            return template.queryForObject(GET_STUDY_GROUP_BY_IDS, getStudyGroupRowMapper(), studyId, studyGroupId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<StudyGroup> listStudyGroupsOrderedByStudyGroupIdAsc(long studyId) {
        return template.query(LIST_STUDY_GROUPS_ORDER_BY_STUDY_GROUP_ID, getStudyGroupRowMapper(), studyId);
    }

    public StudyGroup update(StudyGroup studyGroup) {
        namedTemplate.update(UPDATE_STUDY,
                toParams(studyGroup).addValue("study_group_id", studyGroup.getStudyGroupId())
        );
        return getByIds(studyGroup.getStudyId(), studyGroup.getStudyGroupId());
    }

    public void deleteById(long studyId, int studyGroupId) {
        template.update(DELETE_STUDY_GROUP_BY_ID, studyId, studyGroupId);
    }

    private static MapSqlParameterSource toParams(StudyGroup studyGroup) {
        return new MapSqlParameterSource()
                .addValue("study_id", studyGroup.getStudyId())
                .addValue("title", studyGroup.getTitle())
                .addValue("purpose", studyGroup.getPurpose())
                .addValue("duration", MapperUtils.writeValueAsString(studyGroup.getDuration()));
    }

    private static RowMapper<StudyGroup> getStudyGroupRowMapper() {
        return (rs, rowNum) -> new StudyGroup()
                .setStudyId(rs.getLong("study_id"))
                .setStudyGroupId(rs.getInt("study_group_id"))
                .setTitle(rs.getString("title"))
                .setPurpose(rs.getString("purpose"))
                .setDuration(MapperUtils.readValue(rs.getString("duration"), Duration.class))
                .setCreated(RepositoryUtils.readInstant(rs, "created"))
                .setModified(RepositoryUtils.readInstant(rs, "modified"));
    }

    // for testing purpose only
    protected void clear() {
        template.execute(CLEAR_STUDY_GROUPS);
    }
}
