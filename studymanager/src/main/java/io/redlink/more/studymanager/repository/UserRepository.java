package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.model.MoreUser;
import io.redlink.more.studymanager.model.User;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserRepository {

    private static final String INSERT_USER =
            "INSERT INTO users (user_id,name,institution,email) " +
            "VALUES (:user_id,:name,:institution,:email) " +
            "ON CONFLICT (user_id) DO UPDATE SET name = excluded.name, institution = excluded.institution, email = excluded.email, updated = now() " +
            "RETURNING *";
    private static final String GET_USER_BY_ID = "SELECT * FROM users WHERE user_id = :user_id";
    private static final String DELETE_BY_ID = "DELETE FROM users WHERE user_id = :user_id";

    private final NamedParameterJdbcTemplate namedTemplate;

    public UserRepository(NamedParameterJdbcTemplate template) {
        this.namedTemplate = template;

    }

    public MoreUser save(User user) {
        return namedTemplate.queryForObject(INSERT_USER, toParams(user), getUserRowMapper());
    }

    public void deleteById(String userId) {
        namedTemplate.update(DELETE_BY_ID, Map.of("user_id", userId));
    }

    public Optional<MoreUser> getById(String userId) {
        try (var stream = namedTemplate.queryForStream(GET_USER_BY_ID, Map.of("user_id", userId), getUserRowMapper())) {
            return stream.findFirst();
        }
    }


    private static MapSqlParameterSource toParams(User user) {
        return new MapSqlParameterSource()
                .addValue("user_id", user.id())
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
