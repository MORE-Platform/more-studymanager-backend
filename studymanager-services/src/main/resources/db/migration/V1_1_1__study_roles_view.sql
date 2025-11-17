-- Improve access to study_acl
CREATE INDEX study_acl_study_id ON study_acl (study_id);
CREATE INDEX study_acl_study_id_user_id ON study_acl (study_id, user_id);

-- Specialized VIEW on study_acl for simplified role-retrieval
CREATE VIEW study_roles_by_user AS
SELECT study_id, user_id, array_agg(user_role) as user_roles
FROM study_acl
GROUP BY study_id, user_id;
