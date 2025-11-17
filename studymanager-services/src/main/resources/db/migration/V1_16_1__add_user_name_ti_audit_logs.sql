-- add a column to store the username to the audit_logs table
ALTER TABLE audit_logs
    ADD user_name VARCHAR;