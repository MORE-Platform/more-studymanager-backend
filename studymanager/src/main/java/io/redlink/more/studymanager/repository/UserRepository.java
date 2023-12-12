/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.model.MoreUser;
import io.redlink.more.studymanager.model.SearchResult;
import io.redlink.more.studymanager.model.User;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserRepository {

    private static final String INSERT_USER =
            "INSERT INTO users (user_id,name,institution,email) " +
            "VALUES (:user_id,:name,:institution,:email) " +
            "ON CONFLICT (user_id) DO UPDATE SET name = excluded.name, institution = excluded.institution, email = excluded.email, updated = now() " +
            "RETURNING *";
    private static final String GET_USER_BY_ID = "SELECT * FROM users WHERE user_id = :user_id";
    private static final String DELETE_BY_ID = "DELETE FROM users WHERE user_id = :user_id";
    private static final String FIND_USERS =
            "SELECT * FROM users " +
            "WHERE (LOWER(institution) = LOWER(:institution)) AND (LOWER(name) LIKE LOWER(:query) OR LOWER(email) LIKE LOWER(:query)) " +
            "ORDER BY user_id LIMIT :limit OFFSET :skip";
    private static final String COUNT_USERS =
            "SELECT count(*) as count FROM users " +
            "WHERE (LOWER(institution) = LOWER(:institution)) AND (LOWER(name) LIKE LOWER(:query) OR LOWER(email) LIKE LOWER(:query))";

    private final NamedParameterJdbcTemplate namedTemplate;

    public UserRepository(NamedParameterJdbcTemplate template) {
        this.namedTemplate = template;

    }

    public MoreUser save(User user) {
        return namedTemplate.queryForObject(INSERT_USER, toParams(user), getUserRowMapper());
    }

    public void deleteById(String userId) {
        namedTemplate.update(DELETE_BY_ID, toParams(userId));
    }

    public Optional<MoreUser> getById(String userId) {
        try (var stream = namedTemplate.queryForStream(GET_USER_BY_ID, toParams(userId), getUserRowMapper())) {
            return stream.findFirst();
        }
    }


    @Transactional(readOnly = true)
    public SearchResult<MoreUser> findUser(String query, String institution, int offset, int limit) {
        String sqlLike = StringUtils.defaultString(query).replace("%", "");
        if (sqlLike.isEmpty()) {
            return new SearchResult<>();
        } else {
            sqlLike = StringUtils.wrap(sqlLike, '%');
        }

        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("query", sqlLike)
                .addValue("institution", institution)
                .addValue("skip", offset)
                .addValue("limit", limit);
        final Long count = namedTemplate.queryForObject(COUNT_USERS, params, ((rs, i) -> rs.getLong("count")));
        if (count == null || count == 0) {
            return new SearchResult<>();
        }

        return new SearchResult<>(
                count,
                offset,
                namedTemplate.query(
                        FIND_USERS, params, getUserRowMapper()
                ));
    }

    private static MapSqlParameterSource toParams(String userId) {
        return new MapSqlParameterSource()
                .addValue("user_id", userId);
    }

    private static MapSqlParameterSource toParams(User user) {
        return toParams(user.id())
                .addValue("name", user.fullName())
                .addValue("institution", user.institution())
                .addValue("email", user.email())
                ;
    }

    private static RowMapper<MoreUser> getUserRowMapper() {
        return (rs, rowNum) -> new MoreUser(
                rs.getString("user_id"),
                rs.getString("name"),
                rs.getString("institution"),
                rs.getString("email"),
                RepositoryUtils.readInstant(rs, "inserted"),
                RepositoryUtils.readInstant(rs, "updated")
        );
    }

}
