package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.model.Intervention;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

@Component
public class InterventionRepository {

    private static final String INSERT_INTERVENTION = "INSERT INTO interventions(study_id,intervention_id,title,purpose,study_group_id) VALUES (:study_id,(SELECT COALESCE(MAX(study_group_id),0)+1 FROM study_groups WHERE study_id = :study_id),:title,:purpose,:study_group_id)";

    private final JdbcTemplate template;
    private final NamedParameterJdbcTemplate namedTemplate;

    public InterventionRepository(JdbcTemplate template) {
        this.template = template;
        this.namedTemplate = new NamedParameterJdbcTemplate(template);
    }

    public Intervention insert(Intervention intervention) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            namedTemplate.update(INSERT_INTERVENTION, toParams(intervention), keyHolder, new String[] { "intervention_id" });
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("Study " + intervention.getStudyId() + " does not exist");
        }
        return getByIds(intervention.getStudyId(), keyHolder.getKey().intValue());
    }

    private Intervention getByIds(Long studyId, Integer interventionId) {
        return null;
    }

    private static MapSqlParameterSource toParams(Intervention intervention) {
        return null;
    }

}
