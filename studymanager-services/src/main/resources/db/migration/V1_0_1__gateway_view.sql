-- cleanup unused view
DROP VIEW gateway_participant_details;


CREATE VIEW auth_routing_info (api_id, api_secret, study_id, participant_id, study_group_id) AS
SELECT api_credentials.*, pt.study_group_id
FROM api_credentials
         INNER JOIN participants pt
                    ON (api_credentials.study_id = pt.study_id and api_credentials.participant_id = pt.participant_id);
