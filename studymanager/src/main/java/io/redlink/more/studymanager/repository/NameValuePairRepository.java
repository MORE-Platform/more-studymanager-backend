package io.redlink.more.studymanager.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.io.Serializable;
import java.util.Optional;

@Component
public class NameValuePairRepository {

    private static final String UPSERT = "INSERT INTO nvpairs(issuer,name,value) VALUES (?,?,?) ON CONFLICT(issuer,name) DO UPDATE SET value = EXCLUDED.value";
    private static final String READ = "SELECT value FROM nvpairs WHERE issuer = ? AND name = ? LIMIT 1";
    private static final String REMOVE = "DELETE FROM nvpairs WHERE issuer = ? AND name = ?";

    private final JdbcTemplate template;

    public NameValuePairRepository(JdbcTemplate template) {
        this.template = template;
    }

    public <T extends Serializable> void setValue(String issuer, String name, T value) {
        this.template.update(UPSERT, issuer, name, SerializationUtils.serialize(value));
    }

    public <T extends Serializable> Optional<T> getValue(String issuer, String name, Class<T> tClass) {
        try {
            return Optional.ofNullable(this.template.queryForObject(READ,
                    (rs, rowNum) -> tClass.cast(SerializationUtils.deserialize(rs.getBytes("value"))),
                    issuer, name));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void removeValue(String issuer, String name) {
        this.template.update(REMOVE, issuer, name);
    }

    void clear() {
        this.template.execute("DELETE FROM nvpairs");
    }
}
