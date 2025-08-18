/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.model.Contact;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.model.User;
import io.redlink.more.studymanager.model.scheduler.Duration;
import io.redlink.more.studymanager.utils.MapperUtils;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class StudyRepository {

    private static final String INSERT_STUDY =
            "INSERT INTO studies (title,purpose,participant_info,consent_info,finish_text,planned_start_date,planned_end_date,duration,institute,contact_person,contact_email,contact_phone) " +
            "VALUES (:title,:purpose,:participant_info,:consent_info,:finish_text,:planned_start_date,:planned_end_date,:duration::jsonb,:institute,:contact_person,:contact_email,:contact_phone) " +
            "RETURNING *";
    private static final String GET_STUDY_BY_ID =
            "SELECT *, " +
            "  (SELECT user_roles FROM study_roles_by_user WHERE study_roles_by_user.study_id = studies.study_id AND user_id = :userId) AS user_roles " +
            "FROM studies " +
            "WHERE study_id = :studyId";

    private static final String COUNT_STUDY =
            "SELECT count(*) AS c " +
                    "FROM studies " +
                    "WHERE study_id = ?";
    private static final String LIST_STUDIES_ORDER_BY_MODIFIED_DESC = "SELECT * FROM studies ORDER BY modified DESC";
    private static final String LIST_STUDIES_BY_STATUS = "SELECT * FROM studies WHERE status = ?::study_state";
    private static final String LIST_STUDY_BY_ACL =
            "SELECT studies.*, acl.user_roles " +
            "FROM studies " +
            "    INNER JOIN (SELECT acl.study_id, array_agg(user_role) AS user_roles FROM study_acl acl WHERE acl.user_id = :userId AND acl.user_role IN (:roles) GROUP BY acl.study_id) acl " +
            "    ON (studies.study_id = acl.study_id) " +
            "ORDER BY modified DESC";
    private static final String UPDATE_STUDY =
            "UPDATE studies SET title = :title, purpose = :purpose, participant_info = :participant_info, consent_info = :consent_info, finish_text = :finish_text, planned_start_date = :planned_start_date, " +
                    "planned_end_date = :planned_end_date, duration = :duration::jsonb, modified = now(), institute = :institute, contact_person = :contact_person, contact_email = :contact_email, contact_phone = :contact_phone " +
            "WHERE study_id = :study_id " +
            "RETURNING *, (SELECT user_roles FROM study_roles_by_user WHERE study_roles_by_user.study_id = studies.study_id AND user_id = :userId) AS user_roles";

    private static final String DELETE_BY_ID = "DELETE FROM studies WHERE study_id = ?";
    private static final String CLEAR_STUDIES = "DELETE FROM studies";
    private static final String SET_STUDY_STATE = """
            UPDATE studies
            SET status = :newState::study_state,
                modified = now(),
                start_date = CASE WHEN :setStart = 0 THEN NULL WHEN :setStart = 1 THEN now() ELSE start_date END,
                end_date = CASE WHEN :setEnd = 0 THEN NULL WHEN :setEnd = 1 THEN now() ELSE end_date END
            WHERE study_id = :studyId
            RETURNING *""";
    private static final String STUDY_HAS_STATE = "SELECT study_id FROM studies WHERE study_id = :study_id AND status::varchar IN (:study_status)";

    private final JdbcTemplate template;
    private final NamedParameterJdbcTemplate namedTemplate;

    public StudyRepository(JdbcTemplate template) {
        this.template = template;
        this.namedTemplate = new NamedParameterJdbcTemplate(template);
    }

    public Study insert(Study study) {
        return namedTemplate.queryForObject(INSERT_STUDY, studyToParams(study), getStudyRowMapper());
    }

    public Optional<Study> getById(long id) {
        return getById(id, null);
    }

    public Optional<Study> getById(long id, User user) {
        try (var stream = namedTemplate.queryForStream(GET_STUDY_BY_ID,
                new MapSqlParameterSource()
                        .addValue("studyId", id)
                        .addValue("userId", user != null ? user.id() : null),
                getStudyRowMapperWithUserRoles())) {
            return stream
                    .findFirst();
        }
    }

    public List<Study> listStudyOrderByModifiedDesc() {
        return template.query(LIST_STUDIES_ORDER_BY_MODIFIED_DESC, getStudyRowMapper());
    }

    public List<Study> listStudiesByAclOrderByModifiedDesc(User user, Set<StudyRole> allowedRoles) {
        return namedTemplate.query(LIST_STUDY_BY_ACL,
                StudyAclRepository.createParams(user.id(), allowedRoles),
                getStudyRowMapperWithUserRoles());
    }

    public Optional<Study> update(Study study, User user) {
        try (var stream = namedTemplate.queryForStream(UPDATE_STUDY,
                studyToParams(study)
                        .addValue("study_id", study.getStudyId())
                        .addValue("userId", user != null ? user.id() : null),
                getStudyRowMapperWithUserRoles()
        )) {
            return stream
                    .findFirst();
        }
    }

    public void deleteById(long id) {
        template.update(DELETE_BY_ID, id);
    }

    public Optional<Study> setStateById(long id, Study.Status status) {
        final int toNull = 0, toNow = 1, keepCurrentValue = -1;
        int setStart = keepCurrentValue, setEnd = keepCurrentValue;
        switch (status) {
            case DRAFT -> {
                setStart = toNull;
                setEnd = toNull;
            }
            case ACTIVE, PREVIEW -> setStart = toNow;
            case CLOSED -> setEnd = toNow;
        }

        try (var stream = namedTemplate.queryForStream(SET_STUDY_STATE,
                new MapSqlParameterSource()
                        .addValue("studyId", id)
                        .addValue("newState", status.getValue())
                        .addValue("setStart", setStart)
                        .addValue("setEnd", setEnd),
                getStudyRowMapper()
        )) {
            return stream
                    .findFirst();
        }
    }

    private static MapSqlParameterSource studyToParams(Study study) {
        return new MapSqlParameterSource()
                .addValue("title", study.getTitle())
                .addValue("purpose", study.getPurpose())
                .addValue("participant_info", study.getParticipantInfo())
                .addValue("consent_info", study.getConsentInfo())
                .addValue("finish_text", study.getFinishText())
                .addValue("planned_start_date", study.getPlannedStartDate())
                .addValue("planned_end_date", study.getPlannedEndDate())
                .addValue("duration", MapperUtils.writeValueAsString(study.getDuration()))
                .addValue("institute", study.getContact().getInstitute())
                .addValue("contact_person", study.getContact().getPerson())
                .addValue("contact_email", study.getContact().getEmail())
                .addValue("contact_phone", study.getContact().getPhoneNumber())
                ;
    }

    private static RowMapper<Study> getStudyRowMapper() {
        return (rs, rowNum) -> new Study()
                .setStudyId(rs.getLong("study_id"))
                .setTitle(rs.getString("title"))
                .setPurpose(rs.getString("purpose"))
                .setFinishText(rs.getString("finish_text"))
                .setParticipantInfo(rs.getString("participant_info"))
                .setConsentInfo(rs.getString("consent_info"))
                .setPlannedStartDate(RepositoryUtils.readLocalDate(rs, "planned_start_date"))
                .setPlannedEndDate(RepositoryUtils.readLocalDate(rs,"planned_end_date"))
                .setStartDate(RepositoryUtils.readLocalDate(rs,"start_date"))
                .setEndDate(RepositoryUtils.readLocalDate(rs,"end_date"))
                .setDuration(MapperUtils.readValue(rs.getString("duration"), Duration.class))
                .setCreated(RepositoryUtils.readInstant(rs, "created"))
                .setModified(RepositoryUtils.readInstant(rs, "modified"))
                .setStudyState(Study.Status.fromValue(rs.getString("status").toUpperCase()))
                .setContact(new Contact()
                        .setInstitute(rs.getString("institute"))
                        .setPerson(rs.getString("contact_person"))
                        .setEmail(rs.getString("contact_email"))
                        .setPhoneNumber(rs.getString("contact_phone")));
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

    public boolean hasState(long studyId, Collection<Study.Status> allowedStates){
        if(allowedStates.isEmpty())
            return false;
        try(
                var stream = namedTemplate.queryForStream(STUDY_HAS_STATE,
                        new MapSqlParameterSource()
                                .addValue("study_id", studyId)
                                .addValue("study_status", allowedStates.stream().map(Study.Status::getValue).toList()),
                        (rs, rowNum) -> rs.getLong("study_id")
                )) {
            return stream.findFirst().isPresent();
        }
    }

    public Optional<Boolean> exists(Long studyId) {
        return template.query(COUNT_STUDY, (rs, rowNum) -> rs.getInt("c"), studyId).stream()
                .findFirst().map(c -> c == 1);
    }
}
