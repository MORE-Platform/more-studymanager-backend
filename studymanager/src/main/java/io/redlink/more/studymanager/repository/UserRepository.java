package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.model.MoreUser;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserRepository {

    private static final String INSERT_USER = "INSERT INTO users (user_id,name,institution,email,inserted,updated) VALUES (:user_id,:name,:institution,:email,now(),now()) ON CONFLICT (user_id) DO UPDATE SET user_id = :user_id, name = :name, institution = :institution, email = :email, inserted = now(), updated = now()";
    private static final String GET_USER_BY_ID = "SELECT * FROM users WHERE user_id = ?";
    private static final String UPDATE_USER = "UPDATE users SET name = :name, institution = :institution, email = :email, updated = now() WHERE user_id = :user_id";
    private static final String DELETE_BY_ID = "DELETE FROM users WHERE user_id = ?";
    private final JdbcTemplate template;
    private final NamedParameterJdbcTemplate namedTemplate;

    public UserRepository(JdbcTemplate template) {
        this.template = template;
        this.namedTemplate = new NamedParameterJdbcTemplate(template);

    }

    public MoreUser insert(MoreUser user) {
        namedTemplate.update(INSERT_USER, toParams(user));
        return getById(user.id());
    }

    public MoreUser update(MoreUser user){
        namedTemplate.update(UPDATE_USER, toParams(user));
        return getById(user.id());
    }

    public void deleteById(String id){
        template.update(DELETE_BY_ID, id);
    }

    public MoreUser getById(String user_id) {
        try {
            return template.queryForObject(GET_USER_BY_ID, getUserRowMapper(), user_id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }



    private static MapSqlParameterSource toParams(MoreUser user) {
        return new MapSqlParameterSource()
                .addValue("user_id", user.id())
                .addValue("name", user.name())
                .addValue("institution", user.institution())
                .addValue("email", user.email())
                .addValue("inserted", user.inserted())
                .addValue("updated", user.updated())
                ;
    }

    private static RowMapper<MoreUser> getUserRowMapper() {
        return (rs, rowNum) -> new MoreUser(
                rs.getString("user_id"),
                rs.getString("name"),
                rs.getString("institution"),
                rs.getString("email"),
                rs.getTimestamp("inserted"),
                rs.getTimestamp("upadted")

        );
    }
}
