package io.redlink.more.studymanager.model;

import java.sql.Timestamp;

public record Study_ACL(
        long study_id,
        String user_id,
        String user_role,
        Timestamp created,
        String creator_id
) {
}