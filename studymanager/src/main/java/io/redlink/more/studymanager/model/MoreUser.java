package io.redlink.more.studymanager.model;


import java.sql.Timestamp;

public record MoreUser(
        String id,
        String name,
        String institution,
        String email,
        Timestamp inserted,
        Timestamp updated
) {
}
