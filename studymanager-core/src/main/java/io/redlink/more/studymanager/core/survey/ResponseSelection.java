/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache License, Version 2.0.
 */
package io.redlink.more.studymanager.core.survey;

/**
 * Selects which of a participant's survey responses should be returned. A participant may submit the same
 * survey more than once, so a caller has to state whether it wants every response, only the most recent one,
 * or one specific response identified by its id.
 *
 * @param mode    how to select
 * @param savedId the response id, required for {@link Mode#BY_SAVED_ID} and forbidden otherwise
 */
public record ResponseSelection(Mode mode, Integer savedId) {

    public enum Mode {
        ALL,
        LATEST,
        BY_SAVED_ID
    }

    public ResponseSelection {
        if (mode == null) {
            throw new IllegalArgumentException("mode must not be null");
        }
        if (mode == Mode.BY_SAVED_ID && savedId == null) {
            throw new IllegalArgumentException("savedId is required for mode BY_SAVED_ID");
        }
        if (mode != Mode.BY_SAVED_ID && savedId != null) {
            throw new IllegalArgumentException("savedId must only be set for mode BY_SAVED_ID");
        }
    }

    public static ResponseSelection all() {
        return new ResponseSelection(Mode.ALL, null);
    }

    public static ResponseSelection latest() {
        return new ResponseSelection(Mode.LATEST, null);
    }

    public static ResponseSelection bySavedId(int savedId) {
        return new ResponseSelection(Mode.BY_SAVED_ID, savedId);
    }

    public boolean isAll() {
        return mode == Mode.ALL;
    }

    public boolean isLatest() {
        return mode == Mode.LATEST;
    }

    public boolean isBySavedId() {
        return mode == Mode.BY_SAVED_ID;
    }
}
