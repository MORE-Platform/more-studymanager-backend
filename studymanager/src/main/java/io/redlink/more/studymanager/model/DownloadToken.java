package io.redlink.more.studymanager.model;

import java.time.Instant;

public class DownloadToken {
    private String token;
    private Long studyId;
    private String filename;
    private Instant expiry;

    public String getToken() {
        return token;
    }

    public DownloadToken setToken(String token) {
        this.token = token;
        return this;
    }

    public Long getStudyId() {
        return studyId;
    }

    public DownloadToken setStudyId(Long studyId) {
        this.studyId = studyId;
        return this;
    }

    public String getFilename() {
        return filename;
    }

    public DownloadToken setFilename(String filename) {
        this.filename = filename;
        return this;
    }

    public Instant getExpiry() {
        return expiry;
    }

    public DownloadToken setExpiry(Instant expiry) {
        this.expiry = expiry;
        return this;
    }
}
