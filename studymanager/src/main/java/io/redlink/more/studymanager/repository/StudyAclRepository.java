/*
 * Copyright (c) 2022 Redlink GmbH.
 */
package io.redlink.more.studymanager.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class StudyAclRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public StudyAclRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


}
