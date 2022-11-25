/*
 * Copyright (c) 2022 Redlink GmbH.
 */
package io.redlink.more.studymanager.model;

/**
 * The roles a user can have on <strong>study</strong>-level.
 */
public enum StudyRole {

    /**
     * * Read access to study-metadata
     * * Read access to collected data
     */
    STUDY_VIEWER,
    /**
     * * Manage study-metadata
     * * Configure study (observations, interventions, ...)
     */
    STUDY_OPERATOR,
    /**
     * * Manage study-metadata
     * * Configure study (observations, interventions, ...)
     * * Assign collaborators
     */
    STUDY_ADMIN,

    ;

}
