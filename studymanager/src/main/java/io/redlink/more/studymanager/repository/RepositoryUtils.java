/*
 * Copyright (c) 2022 Redlink GmbH.
 */
package io.redlink.more.studymanager.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

public final class RepositoryUtils {

    private RepositoryUtils() {}

    public static Instant readInstant(ResultSet rs, String columnLabel) throws SQLException {
        var timestamp = rs.getTimestamp(columnLabel);
        if (timestamp == null) {
            return null;
        }
        return timestamp.toInstant();
    }

}
