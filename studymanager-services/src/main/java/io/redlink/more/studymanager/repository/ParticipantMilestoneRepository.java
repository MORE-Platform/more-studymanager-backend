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
import io.redlink.more.studymanager.model.ParticipantMilestone;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;

@Component
public class ParticipantMilestoneRepository {
    private static final String INSERT_PARTICIPANT_MILESTONE = """
            INSERT INTO participant_milestones (study_id, participant_id, milestone_id, participant_milestone_id, date_time)
            VALUES (:study_id, :participant_id, :milestone_id,
                    (SELECT COALESCE(MAX(participant_milestone_id),0)+1 FROM participant_milestones WHERE study_id = :study_id),
                    :date_time)""";
    private static final String GET_PARTICIPANT_MILESTONE_BY_IDS = """
            SELECT pm.*, m.name AS milestone_name
            FROM participant_milestones pm
                JOIN milestones m ON m.study_id = pm.study_id AND m.milestone_id = pm.milestone_id
            WHERE pm.study_id = ? AND pm.participant_id = ? AND pm.milestone_id = ?""";
    private static final String LIST_PARTICIPANT_MILESTONES = """
            SELECT pm.*, m.name AS milestone_name
            FROM participant_milestones pm
                JOIN milestones m ON m.study_id = pm.study_id AND m.milestone_id = pm.milestone_id
            WHERE pm.study_id = ? AND pm.participant_id = ?
            ORDER BY m.order_index""";
    private static final String UPDATE_PARTICIPANT_MILESTONE = """
            UPDATE participant_milestones SET date_time = :date_time, modified = now()
            WHERE study_id = :study_id AND participant_id = :participant_id AND milestone_id = :milestone_id""";
    private static final String DELETE_PARTICIPANT_MILESTONE = """
            DELETE FROM participant_milestones
            WHERE study_id = ? AND participant_id = ? AND milestone_id = ?""";
    private static final String EXISTS_PARTICIPANT_MILESTONE = """
            SELECT COUNT(*) FROM participant_milestones
            WHERE study_id = :study_id AND participant_id = :participant_id AND milestone_id = :milestone_id""";
    private static final String CLEAR_PARTICIPANT_MILESTONES = "DELETE FROM participant_milestones";

    private final JdbcTemplate template;
    private final NamedParameterJdbcTemplate namedTemplate;

    public ParticipantMilestoneRepository(JdbcTemplate template) {
        this.template = template;
        this.namedTemplate = new NamedParameterJdbcTemplate(template);
    }

    public ParticipantMilestone insert(ParticipantMilestone participantMilestone) {
        try {
            namedTemplate.update(INSERT_PARTICIPANT_MILESTONE, toParams(participantMilestone));
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException(
                    "Milestone " + participantMilestone.getMilestoneId() + " or participant " +
                            participantMilestone.getParticipantId() + " does not exist in study " +
                            participantMilestone.getStudyId());
        }
        return getByIds(participantMilestone.getStudyId(), participantMilestone.getParticipantId(), participantMilestone.getMilestoneId());
    }

    public ParticipantMilestone getByIds(long studyId, int participantId, int milestoneId) {
        try {
            return template.queryForObject(GET_PARTICIPANT_MILESTONE_BY_IDS, getRowMapper(), studyId, participantId, milestoneId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<ParticipantMilestone> listByParticipant(long studyId, int participantId) {
        return template.query(LIST_PARTICIPANT_MILESTONES, getRowMapper(), studyId, participantId);
    }

    public ParticipantMilestone update(ParticipantMilestone participantMilestone) {
        namedTemplate.update(UPDATE_PARTICIPANT_MILESTONE, toParams(participantMilestone));
        return getByIds(participantMilestone.getStudyId(), participantMilestone.getParticipantId(), participantMilestone.getMilestoneId());
    }

    public void deleteByIds(long studyId, int participantId, int milestoneId) {
        template.update(DELETE_PARTICIPANT_MILESTONE, studyId, participantId, milestoneId);
    }

    public boolean exists(long studyId, int participantId, int milestoneId) {
        Integer count = namedTemplate.queryForObject(
                EXISTS_PARTICIPANT_MILESTONE,
                new MapSqlParameterSource()
                        .addValue("study_id", studyId)
                        .addValue("participant_id", participantId)
                        .addValue("milestone_id", milestoneId),
                Integer.class);
        return count != null && count > 0;
    }

    private static MapSqlParameterSource toParams(ParticipantMilestone participantMilestone) {
        return new MapSqlParameterSource()
                .addValue("study_id", participantMilestone.getStudyId())
                .addValue("participant_id", participantMilestone.getParticipantId())
                .addValue("milestone_id", participantMilestone.getMilestoneId())
                .addValue("date_time", participantMilestone.getDateTime() != null ? Timestamp.from(participantMilestone.getDateTime()) : null);
    }

    private static RowMapper<ParticipantMilestone> getRowMapper() {
        return (rs, rowNum) -> new ParticipantMilestone()
                .setStudyId(rs.getLong("study_id"))
                .setParticipantId(rs.getInt("participant_id"))
                .setMilestoneId(rs.getInt("milestone_id"))
                .setParticipantMilestoneId(rs.getInt("participant_milestone_id"))
                .setName(rs.getString("milestone_name"))
                .setDateTime(RepositoryUtils.readInstant(rs, "date_time"))
                .setCreated(RepositoryUtils.readInstant(rs, "created"))
                .setModified(RepositoryUtils.readInstant(rs, "modified"));
    }

    // for testing purpose only
    protected void clear() {
        template.execute(CLEAR_PARTICIPANT_MILESTONES);
    }
}
