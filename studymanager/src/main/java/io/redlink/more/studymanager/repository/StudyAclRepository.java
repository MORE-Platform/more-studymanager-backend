/*
 * Copyright (c) 2022 Redlink GmbH.
 */
package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.model.MoreUser;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.model.StudyUserRoles;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.redlink.more.studymanager.repository.RepositoryUtils.readInstant;

@Service
public class StudyAclRepository {

    private static final String SQL_UPDATE_ROLES =
            "WITH new_roles AS (" +
            "  SELECT :studyId as study_id, :userId as user_id, role as user_role, :creator as creator_id " +
            "  FROM unnest(ARRAY[ :roles ]) as role" +
            ") " +
            "INSERT INTO study_acl(study_id, user_id, user_role, creator_id) " +
            "SELECT * FROM new_roles " +
            "ON CONFLICT (study_id, user_id, user_role) DO NOTHING " +
            "RETURNING user_role";
    private static final String SQL_RETAIN_ROLES =
            "DELETE FROM study_acl " +
            "WHERE study_id = :studyId AND user_id = :userId AND user_role NOT IN (:roles)";
    private static final String COUNT_ROLES =
            "SELECT count(*) FROM study_acl " +
            "WHERE study_id = :studyId AND user_id = :userId AND user_role IN (:roles)";
    private static final String LIST_ROLES =
            "SELECT user_role FROM study_acl " +
            "WHERE study_id = :studyId AND user_id = :userId";
    private static final String CLEAR_ROLES =
            "DELETE FROM study_acl " +
            "WHERE study_id = :studyId AND user_id = :userId";
    private static final String LIST_ACL_FOR_STUDY =
            "SELECT users.*, acl.roles " +
            "FROM users " +
            "    INNER JOIN (" +
            "        SELECT sa.user_id, array_agg(sa.user_role) AS roles " +
            "        FROM study_acl sa " +
            "        WHERE study_id = :studyId " +
            "        GROUP BY sa.user_id) acl " +
            "    ON (users.user_id = acl.user_id)" +
            "";
    public static final String GET_ROLE_DETAILS =
            "SELECT user_role, created, users.* " +
            "FROM study_acl LEFT OUTER JOIN users ON (study_acl.creator_id = users.user_id) " +
            "WHERE study_id = :studyId AND study_acl.user_id = :userId";


    private final NamedParameterJdbcTemplate jdbcTemplate;

    public StudyAclRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<MoreUser, Set<StudyRole>> getACL(Study study) {
        final Long studyId = study.getStudyId();
        if (studyId == null) return Map.of();
        return getACL(studyId);
    }

    public Map<MoreUser, Set<StudyRole>> getACL(long studyId) {
        var acl = new HashMap<MoreUser, Set<StudyRole>>();
        jdbcTemplate.query(LIST_ACL_FOR_STUDY,
                createParams(studyId, "_unused_"),
                ((rs, rowNum) ->
                        Map.entry(
                                readUser(rs),
                                readRoleArray(rs, "roles")
                        )
                )
        ).forEach(e -> acl.put(e.getKey(), e.getValue()));
        return Map.copyOf(acl);
    }

    public Set<StudyRole> getRoles(long studyId, String userId) {
        try (var steam = jdbcTemplate.queryForStream(LIST_ROLES,
                createParams(studyId, userId),
                (rs, i) -> readRole(rs, "user_role")
        )) {
            return steam
                    .filter(Objects::nonNull)
                    .collect(Collectors.toUnmodifiableSet());
        }
    }

    public boolean hasRole(long studyId, String userId, StudyRole role) {
        return hasAllRoles(studyId, userId, role);
    }

    public boolean hasAllRoles(long studyId, String userId, StudyRole... roles) {
        return hasAllRoles(studyId, userId, Set.of(roles));
    }

    public boolean hasAllRoles(long studyId, String userId, Set<StudyRole> roles) {
        return Integer.valueOf(roles.size()).equals(jdbcTemplate.queryForObject(COUNT_ROLES,
                createParams(studyId, userId, roles),
                (rs, i) -> rs.getInt(1)
        ));
    }

    public boolean hasAnyRole(long studyId, String userId, StudyRole... roles) {
        return hasAnyRole(studyId, userId, Set.of(roles));
    }

    public boolean hasAnyRole(long studyId, String userId, Set<StudyRole> roles) {
        return 0 < Optional.ofNullable(jdbcTemplate.queryForObject(COUNT_ROLES,
                createParams(studyId, userId, roles),
                (rs, i) -> rs.getInt(1)
        )).orElse(0);
    }

    @Transactional
    public Set<StudyRole> setRoles(long studyId, String userId, String creatorId, StudyRole... roles) {
        return setRoles(studyId, userId, Set.of(roles), creatorId);
    }

    @Transactional
    public Set<StudyRole> setRoles(long studyId, String userId, Set<StudyRole> roles, String creatorId) {
        var paramMap = createParams(studyId, userId, roles)
                .addValue("creator", creatorId);

        jdbcTemplate.update(SQL_RETAIN_ROLES, paramMap);
        try (var stream = jdbcTemplate.queryForStream(SQL_UPDATE_ROLES,
                paramMap,
                (rs, row) -> readRole(rs, "user_role")
        )) {
            return stream
                    .filter(Objects::nonNull)
                    .collect(Collectors.toUnmodifiableSet());
        }
    }

    @Transactional
    public void clearRoles(long studyId, String userId) {
        jdbcTemplate.update(CLEAR_ROLES, createParams(studyId, userId));
    }

    public Set<StudyUserRoles.StudyRoleDetails> getRoleDetails(long studyId, String userId) {
        try (var steam = jdbcTemplate.queryForStream(GET_ROLE_DETAILS,
                createParams(studyId, userId),
                (rs, row) -> new StudyUserRoles.StudyRoleDetails(
                        readRole(rs, "user_role"),
                        readUser(rs),
                        readInstant(rs, "created")
                )
        )) {
            return steam.collect(Collectors.toUnmodifiableSet());
        }
    }


    static MapSqlParameterSource createParams(long studyId, String userId, Set<StudyRole> roles) {
        return createParams(studyId, userId)
                .addValue("roles", roles.stream().map(Enum::name).toList())
                ;
    }

    static MapSqlParameterSource createParams(long studyId, String userId) {
        return new MapSqlParameterSource()
                .addValue("studyId", studyId)
                .addValue("userId", userId)
                ;
    }

    static MapSqlParameterSource createParams(String userId, Set<StudyRole> roles) {
        return new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("roles", roles.stream().map(Enum::name).toList())
                ;
    }

    private static MoreUser readUser(ResultSet rs) throws SQLException {
        return new MoreUser(
                rs.getString("user_id"),
                rs.getString("name"),
                rs.getString("institution"),
                rs.getString("email"),
                readInstant(rs, "inserted"),
                readInstant(rs, "updated")
        );
    }

    static Set<StudyRole> readRoleArray(ResultSet rs, String columnLabel) throws SQLException {
        final Array array = rs.getArray(columnLabel);
        if (array == null) return Set.of();

        try (var arrayRs = array.getResultSet()) {
            var roles = EnumSet.noneOf(StudyRole.class);
            while (arrayRs.next()) {
                var role = readRole(arrayRs, "value");
                if (role != null) {
                    roles.add(role);
                }
            }
            return Set.copyOf(roles);
        }
    }

    static StudyRole readRole(ResultSet rs, String columnLabel) throws SQLException {
        try {
            return StudyRole.valueOf(rs.getString(columnLabel));
        } catch (IllegalArgumentException e) {
            // Invalid Mapping, ignore it!
            return null;
        }
    }

}
