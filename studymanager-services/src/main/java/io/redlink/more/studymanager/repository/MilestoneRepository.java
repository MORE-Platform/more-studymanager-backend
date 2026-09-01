/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache License, Version 2.0.
 */
package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.model.Milestone;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MilestoneRepository {
    private static final String INSERT_MILESTONE = """
            INSERT INTO milestones (study_id, milestone_id, name, order_index)
            VALUES (:study_id,
                    (SELECT COALESCE(MAX(milestone_id),0)+1 FROM milestones WHERE study_id = :study_id),
                    :name,
                    (SELECT COALESCE(MAX(order_index),0)+1 FROM milestones WHERE study_id = :study_id))
            RETURNING *""";
    private static final String GET_MILESTONE_BY_IDS = "SELECT * FROM milestones WHERE study_id = ? AND milestone_id = ?";
    private static final String LIST_MILESTONES_ORDER_BY_ORDER_INDEX = "SELECT * FROM milestones WHERE study_id = ? ORDER BY order_index";
    private static final String UPDATE_MILESTONE = "UPDATE milestones SET name = :name WHERE study_id = :study_id AND milestone_id = :milestone_id";
    private static final String DELETE_MILESTONE_BY_ID = "DELETE FROM milestones WHERE study_id = ? AND milestone_id = ?";
    private static final String DECREMENT_ORDER_INDEX_ABOVE = """
            UPDATE milestones SET order_index = order_index - 1
            WHERE study_id = :study_id AND order_index > :order_index""";
    private static final String SHIFT_ORDER_INDEX_RANGE = """
            UPDATE milestones SET order_index = order_index + :delta
            WHERE study_id = :study_id AND order_index BETWEEN :from_index AND :to_index""";
    private static final String SET_ORDER_INDEX = """
            UPDATE milestones SET order_index = :order_index
            WHERE study_id = :study_id AND milestone_id = :milestone_id""";
    private static final String COUNT_MILESTONES = "SELECT COUNT(*) FROM milestones WHERE study_id = :study_id";
    private static final String COUNT_ACTIVE_PARTICIPANT_MILESTONES = """
            SELECT COUNT(*) FROM participant_milestones pm
            JOIN participants p ON p.study_id = pm.study_id AND p.participant_id = pm.participant_id
            WHERE pm.study_id = :study_id AND pm.milestone_id = :milestone_id AND p.status = 'active'""";
    private static final String CLEAR_MILESTONES = "DELETE FROM milestones";

    private final JdbcTemplate template;
    private final NamedParameterJdbcTemplate namedTemplate;

    public MilestoneRepository(JdbcTemplate template) {
        this.template = template;
        this.namedTemplate = new NamedParameterJdbcTemplate(template);
    }

    public Milestone insert(Milestone milestone) {
        try {
            return namedTemplate.queryForObject(INSERT_MILESTONE, toParams(milestone), getMilestoneRowMapper());
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("Study " + milestone.getStudyId() + " does not exist");
        }
    }

    public Milestone getByIds(long studyId, int milestoneId) {
        try {
            return template.queryForObject(GET_MILESTONE_BY_IDS, getMilestoneRowMapper(), studyId, milestoneId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Milestone> listMilestonesOrderedByOrderIndexAsc(long studyId) {
        return template.query(LIST_MILESTONES_ORDER_BY_ORDER_INDEX, getMilestoneRowMapper(), studyId);
    }

    public Milestone update(Milestone milestone) {
        namedTemplate.update(UPDATE_MILESTONE,
                new MapSqlParameterSource()
                        .addValue("study_id", milestone.getStudyId())
                        .addValue("milestone_id", milestone.getMilestoneId())
                        .addValue("name", milestone.getName())
        );
        return getByIds(milestone.getStudyId(), milestone.getMilestoneId());
    }

    public void deleteById(long studyId, int milestoneId) {
        template.update(DELETE_MILESTONE_BY_ID, studyId, milestoneId);
    }

    public void decrementOrderIndexAbove(long studyId, int orderIndex) {
        namedTemplate.update(DECREMENT_ORDER_INDEX_ABOVE,
                new MapSqlParameterSource()
                        .addValue("study_id", studyId)
                        .addValue("order_index", orderIndex)
        );
    }

    public void shiftOrderIndexRange(long studyId, int fromIndexInclusive, int toIndexInclusive, int delta) {
        namedTemplate.update(SHIFT_ORDER_INDEX_RANGE,
                new MapSqlParameterSource()
                        .addValue("study_id", studyId)
                        .addValue("from_index", fromIndexInclusive)
                        .addValue("to_index", toIndexInclusive)
                        .addValue("delta", delta)
        );
    }

    public void setOrderIndex(long studyId, int milestoneId, int orderIndex) {
        namedTemplate.update(SET_ORDER_INDEX,
                new MapSqlParameterSource()
                        .addValue("study_id", studyId)
                        .addValue("milestone_id", milestoneId)
                        .addValue("order_index", orderIndex)
        );
    }

    public int countByStudyId(long studyId) {
        return namedTemplate.queryForObject(
                COUNT_MILESTONES,
                new MapSqlParameterSource().addValue("study_id", studyId),
                Integer.class);
    }

    public int countActiveParticipantMilestones(long studyId, int milestoneId) {
        return namedTemplate.queryForObject(
                COUNT_ACTIVE_PARTICIPANT_MILESTONES,
                new MapSqlParameterSource()
                        .addValue("study_id", studyId)
                        .addValue("milestone_id", milestoneId),
                Integer.class);
    }

    private static MapSqlParameterSource toParams(Milestone milestone) {
        return new MapSqlParameterSource()
                .addValue("study_id", milestone.getStudyId())
                .addValue("name", milestone.getName());
    }

    private static RowMapper<Milestone> getMilestoneRowMapper() {
        return (rs, rowNum) -> new Milestone()
                .setStudyId(rs.getLong("study_id"))
                .setMilestoneId(rs.getInt("milestone_id"))
                .setName(rs.getString("name"))
                .setOrderIndex(rs.getInt("order_index"))
                .setCreated(RepositoryUtils.readInstant(rs, "created"));
    }

    // for testing purpose only
    protected void clear() {
        template.execute(CLEAR_MILESTONES);
    }
}
