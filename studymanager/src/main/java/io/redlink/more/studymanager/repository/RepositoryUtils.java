/*
 * Copyright (c) 2022 Redlink GmbH.
 */
package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.model.Participant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.jdbc.core.RowMapper;

public final class RepositoryUtils {

    private static final Logger LOG = LoggerFactory.getLogger(RepositoryUtils.class);

    private RepositoryUtils() {}

    public static Instant readInstant(ResultSet rs, String columnLabel) throws SQLException {
        var timestamp = rs.getTimestamp(columnLabel);
        if (timestamp == null) {
            return null;
        }
        return timestamp.toInstant();
    }

    public static LocalDate readLocalDate(ResultSet rs, String columnLabel) throws SQLException{
        var date = rs.getDate(columnLabel);
        if(date == null) {
            return null;
        }
        return date.toLocalDate();
    }

    public static Integer getValidNullableIntegerValue(ResultSet rs, String strColName) throws SQLException {
        final int nValue = rs.getInt(strColName);
        return rs.wasNull() ? null : nValue;
    }

    public static Participant.Status readParticipantStatus(ResultSet rs, String columnLabel) throws SQLException {
        final String val = rs.getString(columnLabel);
        if (val == null) {
            return null;
        }

        return switch (val) {
            case "new" -> Participant.Status.NEW;
            case "active" -> Participant.Status.ACTIVE;
            case "abandoned" -> Participant.Status.ABANDONED;
            case "kicked_out" -> Participant.Status.KICKED_OUT;
            case "locked" -> Participant.Status.LOCKED;
            default -> {
                LOG.warn("Unknown Participant-Status {} in column {}, ignoring", val, columnLabel);
                yield null;
            }
        };
    }

    public static String toParam(Participant.Status status) {
        if (status == null) return null;

        return switch (status) {
            case NEW -> "new";
            case ACTIVE -> "active";
            case ABANDONED -> "abandoned";
            case KICKED_OUT -> "kicked_out";
            case LOCKED -> "locked";
        };
    }

    public static RowMapper<Integer> intReader(String columnLabel) {
        return (rs, rowNum) -> {
            int anInt = rs.getInt(columnLabel);
            if (rs.wasNull()) return null;
            return anInt;
        };
    }
}
