/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.core.sdk;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

public interface MorePlatformSDK {

    public enum ParticipantFilter {
        ALL, ACTIVE_ONLY
    }
    <T extends Serializable> void setValue(String name, T value);
    <T extends Serializable> Optional<T> getValue(String name, Class<T> tClass);
    void removeValue(String name);
    Set<Integer> participantIds(ParticipantFilter filter);
    long getStudyId();
    Integer getStudyGroupId();
    // TODO
    /*
    * webhook(id, (params) -> ...)
    * pushNotify(participant, message)
    * kibanaRule(rule, hookId)
    * */
}
