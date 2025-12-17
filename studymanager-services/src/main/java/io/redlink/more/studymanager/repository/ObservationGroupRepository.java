/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.model.ObservationGroup;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ObservationGroupRepository {
    private static final String INSERT_OBSERVATION_GROUP = "INSERT INTO observation_groups (study_id,observation_group_id,title,purpose) VALUES (:study_id,(SELECT COALESCE(MAX(observation_group_id),0)+1 FROM observation_groups WHERE study_id = :study_id),:title,:purpose) RETURNING *";
    private static final String IMPORT_OBSERVATION_GROUP = "INSERT INTO observation_groups (study_id, observation_group_id, title, purpose) VALUES (:study_id,:observation_group_id,:title,:purpose) RETURNING *";
    private static final String GET_OBSERVATION_GROUP_BY_IDS = "SELECT * FROM observation_groups WHERE study_id = ? AND observation_group_id = ?";
    private static final String LIST_OBSERVATION_GROUPS_ORDER_BY_OBSERVATION_GROUP_ID = "SELECT * FROM observation_groups WHERE study_id = ? ORDER BY observation_group_id";
    private static final String UPDATE_OBSERVATION_GROUP =
            "UPDATE observation_groups SET title = :title, purpose = :purpose, modified = now() WHERE study_id = :study_id AND observation_group_id = :observation_group_id";

    private static final String DELETE_OBSERVATION_GROUP_BY_ID = "DELETE FROM observation_groups WHERE study_id = ? AND observation_group_id = ?";
    private static final String CLEAR_OBSERVATION_GROUPS = "DELETE FROM observation_groups";

    private final JdbcTemplate template;
    private final NamedParameterJdbcTemplate namedTemplate;

    public ObservationGroupRepository(JdbcTemplate template) {
        this.template = template;
        this.namedTemplate = new NamedParameterJdbcTemplate(template);
    }

    public ObservationGroup insert(ObservationGroup observationGroup) {
        try {
            return namedTemplate.queryForObject(INSERT_OBSERVATION_GROUP, toParams(observationGroup), getObservationGroupRowMapper());
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("Study " + observationGroup.getStudyId() + " does not exist");
        }
    }

    public ObservationGroup doImport(Long studyId, ObservationGroup observationGroup) {
        try {
            return namedTemplate.queryForObject(
                    IMPORT_OBSERVATION_GROUP,
                    toParams(observationGroup)
                            .addValue("study_id", studyId)
                            .addValue("observation_group_id", observationGroup.getObservationGroupId()),
                    getObservationGroupRowMapper()
            );
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException(
                    "Error during import of observationGroup " +
                            observationGroup.getObservationGroupId() +
                            "for study " +
                            observationGroup.getStudyId()
            );
        }
    }

    public ObservationGroup getByIds(long studyId, int observationGroupId) {
        try {
            return template.queryForObject(GET_OBSERVATION_GROUP_BY_IDS, getObservationGroupRowMapper(), studyId, observationGroupId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<ObservationGroup> listObservationGroupsOrderedByObservationGroupIdAsc(long studyId) {
        return template.query(LIST_OBSERVATION_GROUPS_ORDER_BY_OBSERVATION_GROUP_ID, getObservationGroupRowMapper(), studyId);
    }

    public ObservationGroup update(ObservationGroup studyGroup) {
        namedTemplate.update(UPDATE_OBSERVATION_GROUP,
                toParams(studyGroup).addValue("observation_group_id", studyGroup.getObservationGroupId())
        );
        return getByIds(studyGroup.getStudyId(), studyGroup.getObservationGroupId());
    }

    public void deleteById(long studyId, int observationGroupId) {
        template.update(DELETE_OBSERVATION_GROUP_BY_ID, studyId, observationGroupId);
    }

    private static MapSqlParameterSource toParams(ObservationGroup observationGroup) {
        return new MapSqlParameterSource()
                .addValue("study_id", observationGroup.getStudyId())
                .addValue("title", observationGroup.getTitle())
                .addValue("purpose", observationGroup.getPurpose());
    }

    private static RowMapper<ObservationGroup> getObservationGroupRowMapper() {
        return (rs, rowNum) -> new ObservationGroup()
                .setStudyId(rs.getLong("study_id"))
                .setObservationGroupId(rs.getInt("observation_group_id"))
                .setTitle(rs.getString("title"))
                .setPurpose(rs.getString("purpose"))
                .setCreated(RepositoryUtils.readInstant(rs, "created"))
                .setModified(RepositoryUtils.readInstant(rs, "modified"));
    }

    // for testing purpose only
    protected void clear() {
        template.execute(CLEAR_OBSERVATION_GROUPS);
    }
}
