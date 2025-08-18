package io.redlink.more.studymanager.model.audit;


import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AuditLog {

    public enum ActionState {
        /**
         * 2xx Responses or not <code>null</code> return values
         */
        success,
        /**
         * 3xx Responses
         */
        redirect,
        /**
         * 4xx or 5xx responses, exceptions or <code>null</code> return values
         */
        error,
        /**
         * other such as 1xx responses
         */
        unknown
    }

    private Long id;
    private Instant created;
    private String userId; // User Identifier
    private Long studyId;
    private String action; // Action Taken
    private ActionState actionState = ActionState.unknown; // The state of the aucited Action
    private Instant timestamp; // Timestamp of the Action
    private String resource; // Resource Accessed
    private Map<String, Object> details = new HashMap<>();// Additional Details

    /**
     * Constructor to create new AuditLogs
     * @param userId the user - MUST NOT be <code>null</code>
     * @param studyId the affected study - MUST NOT be <code>null</code>
     * @param action the performed action - MUST NOT be <code>null</code>
     * @param timestamp the timestamp or <code>null</code> to use the current time
     */
    public AuditLog(String userId, Long studyId, String action, Instant timestamp) {
        this.id = null; // to be auto generated
        this.created = null;
        this.studyId = Objects.requireNonNull(studyId);
        this.userId = Objects.requireNonNull(userId);
        this.action = Objects.requireNonNull(action);
        this.timestamp = timestamp == null ? Instant.now() : timestamp;
    }

    /**
     * Constructor to create a AuditLog from data stored in the repository
     * @param id DB generated id
     * @param created DB generated created timestamp
     * @param userId
     * @param studyId
     * @param action
     * @param timestamp
     */
    public AuditLog(Long id, Instant created, String userId, Long studyId, String action, Instant timestamp) {
        this.id = id;
        this.created = created;
        this.userId = Objects.requireNonNull(userId);
        this.studyId = Objects.requireNonNull(studyId);
        this.action = Objects.requireNonNull(action);
        this.timestamp = Objects.requireNonNull(timestamp);
    }

    public Long getId() {
        return id;
    }

    public Instant getCreated() {
        return created;
    }

    public String getUserId() {
        return userId;
    }

    public String getAction() {
        return action;
    }

    public AuditLog setActionState(ActionState actionState) {
        this.actionState = actionState;
        return this;
    }

    public ActionState getActionState() {
        return actionState;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Long getStudyId() {
        return studyId;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public AuditLog setDetails(Map<String, Object> details) {
        this.details = details == null ? new HashMap<>() : details;
        return this;
    }

    public String getResource() {
        return resource;
    }

    public AuditLog setResource(String resource) {
        this.resource = resource;
        return this;
    }



}
