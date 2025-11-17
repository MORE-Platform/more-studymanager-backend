/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.io.Serializable;
import java.util.Optional;

@Component
public class NameValuePairRepository {

    private static final String UPSERT_O = "INSERT INTO nvpairs_observations(study_id, observation_id, name, value) VALUES (?,?,?,?) ON CONFLICT(study_id, observation_id, name) DO UPDATE SET value = EXCLUDED.value";
    private static final String UPSERT_T = "INSERT INTO nvpairs_triggers(study_id, intervention_id, name, value) VALUES (?,?,?,?) ON CONFLICT(study_id, intervention_id, name) DO UPDATE SET value = EXCLUDED.value";
    private static final String UPSERT_A = "INSERT INTO nvpairs_actions(study_id, intervention_id, action_id, name, value) VALUES (?,?,?,?,?) ON CONFLICT(study_id, intervention_id, action_id, name) DO UPDATE SET value = EXCLUDED.value";
    private static final String READ_O = "SELECT value FROM nvpairs_observations WHERE study_id = ? AND observation_id = ? AND name = ? LIMIT 1";
    private static final String READ_T = "SELECT value FROM nvpairs_triggers WHERE study_id = ? AND intervention_id = ? AND name = ? LIMIT 1";
    private static final String READ_A = "SELECT value FROM nvpairs_actions WHERE study_id = ? AND intervention_id = ? AND action_id = ? AND name = ? LIMIT 1";
    private static final String REMOVE_O = "DELETE FROM nvpairs_observations WHERE study_id = ? AND observation_id = ? AND name = ?";
    private static final String REMOVE_T = "DELETE FROM nvpairs_triggers WHERE study_id = ? AND intervention_id = ? AND name = ?";
    private static final String REMOVE_A = "DELETE FROM nvpairs_actions WHERE study_id = ? AND intervention_id = ? AND action_id = ? AND name = ?";

    private final JdbcTemplate template;

    public NameValuePairRepository(JdbcTemplate template) {
        this.template = template;
    }

    public <T extends Serializable> void setObservationValue(Long studyId, int observationId, String name, T value) {
        this.template.update(UPSERT_O, studyId, observationId, name, SerializationUtils.serialize(value));
    }

    public <T extends Serializable> void setTriggerValue(Long studyId, int interventionId, String name, T value) {
        this.template.update(UPSERT_T, studyId, interventionId, name, SerializationUtils.serialize(value));
    }

    public <T extends Serializable> void setActionValue(Long studyId, int interventionId, int actionId, String name, T value) {
        this.template.update(UPSERT_A, studyId, interventionId, actionId, name, SerializationUtils.serialize(value));
    }

    public <T extends Serializable> Optional<T> getObservationValue(Long studyId, int observationId, String name, Class<T> tClass) {
        try {
            return Optional.ofNullable(this.template.queryForObject(READ_O,
                    (rs, rowNum) -> tClass.cast(SerializationUtils.deserialize(rs.getBytes("value"))),
                    studyId, observationId, name));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public <T extends Serializable> Optional<T> getTriggerValue(Long studyId, int interventionId, String name, Class<T> tClass) {
        try {
            return Optional.ofNullable(this.template.queryForObject(READ_T,
                    (rs, rowNum) -> tClass.cast(SerializationUtils.deserialize(rs.getBytes("value"))),
                    studyId, interventionId, name));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public <T extends Serializable> Optional<T> getActionValue(Long studyId, int interventionId, int actionId, String name, Class<T> tClass) {
        try {
            return Optional.ofNullable(this.template.queryForObject(READ_A,
                    (rs, rowNum) -> tClass.cast(SerializationUtils.deserialize(rs.getBytes("value"))),
                    studyId, interventionId, actionId, name));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void removeObservationValue(Long studyId, int observationId, String name) {
        this.template.update(REMOVE_O, studyId, observationId, name);
    }

    public void removeTriggerValue(Long studyId, int interventionId, String name) {
        this.template.update(REMOVE_T, studyId, interventionId, name);
    }

    public void removeActionValue(Long studyId, int interventionId, int actionId, String name) {
        this.template.update(REMOVE_A, studyId, interventionId, actionId, name);
    }

    protected boolean noObservationValues() {
        return this.template.queryForObject(
                "SELECT count(*) AS c FROM nvpairs_observations", Integer.class) == 0;
    }

    void clear() {
        this.template.execute("DELETE FROM nvpairs_observations");
        this.template.execute("DELETE FROM nvpairs_triggers");
        this.template.execute("DELETE FROM nvpairs_actions");
    }
}
