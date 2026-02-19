-- we need observation groups for users in the gateway
CREATE OR REPLACE VIEW auth_routing_info (
    api_id, api_secret, study_id, participant_id, study_group_id, study_is_active,
    participant_is_active, observation_group_ids
) AS
SELECT
    api_credentials.*,
    pt.study_group_id,
    s.status IN ('active', 'preview'),
    pt.status IN ('active'),
    (
        SELECT array_agg(observation_group_id)
        FROM participant_observation_groups pog
        WHERE pog.study_id = api_credentials.study_id
          AND pog.participant_id = api_credentials.participant_id
    ) AS observation_group_ids
FROM api_credentials
         INNER JOIN participants pt
                    ON api_credentials.study_id = pt.study_id
                        AND api_credentials.participant_id = pt.participant_id
         INNER JOIN studies s
                    ON api_credentials.study_id = s.study_id;