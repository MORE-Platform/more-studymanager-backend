/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.model.DownloadToken;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class DownloadTokenRepository {
    private static final String INSERT_TOKEN =
            "INSERT INTO download_tokens (token,study_id,filename) " +
                    "VALUES (:token,:study_id,:filename) " +
                    "RETURNING *";

    private static final String REMOVE_EXPIRED =
            "DELETE FROM download_tokens WHERE expiry < now()";

    private static final String DELETE_AND_REMOVE =
            "DELETE FROM download_tokens WHERE token = :token RETURNING *";

    private static final String CLEAR_TOKENS = "DELETE FROM download_tokens";
    private final NamedParameterJdbcTemplate namedTemplate;

    private final JdbcTemplate template;

    public DownloadTokenRepository(NamedParameterJdbcTemplate namedTemplate, JdbcTemplate template) {
        this.namedTemplate = namedTemplate;
        this.template = template;
    }

    public DownloadToken createToken(Long studyId) {
        String filename = "study-" + studyId + "." +
                Instant.now().toString().substring(0,19).replace(":", "_") + ".json";
        return namedTemplate.queryForObject(INSERT_TOKEN,
                new MapSqlParameterSource()
                        .addValue("token", UUID.randomUUID().toString())
                        .addValue("study_id", studyId)
                        .addValue("filename", filename),
                getTokenRowMapper());
    }

    public Optional<DownloadToken> getToken(String token) {
        try (var stream = namedTemplate.queryForStream(DELETE_AND_REMOVE,
                new MapSqlParameterSource().addValue("token", token),
                getTokenRowMapper())) {
            return stream.findFirst();
        }
    }

    //remove expired every 5 minutes
    @Scheduled(fixedDelay = 1000 * 60 * 5, initialDelay = 1000 * 60)
    public void removeExpired() {
        template.execute(REMOVE_EXPIRED);
    }

    // for testing purpose only
    protected void clear() {
        template.execute(CLEAR_TOKENS);
    }

    private static RowMapper<DownloadToken> getTokenRowMapper() {
        return (rs, rowNum) -> new DownloadToken()
                .setToken(rs.getString("token"))
                .setStudyId(rs.getLong("study_id"))
                .setFilename(rs.getString("filename"))
                .setExpiry(RepositoryUtils.readInstant(rs, "expiry"));
    }
}
