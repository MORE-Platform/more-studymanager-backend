/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
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
