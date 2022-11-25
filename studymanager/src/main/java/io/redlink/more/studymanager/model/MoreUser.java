package io.redlink.more.studymanager.model;

import java.time.Instant;

public record MoreUser(
        String id,
        String fullName,
        String institution,
        String email,
        Instant inserted,
        Instant updated
) implements User {

    public MoreUser(String id, String fullName, String institution, String email) {
        this(id, fullName, institution, email, null, null);
    }
}
