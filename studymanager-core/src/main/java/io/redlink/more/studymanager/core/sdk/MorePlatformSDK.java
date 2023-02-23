package io.redlink.more.studymanager.core.sdk;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

public interface MorePlatformSDK {
    <T extends Serializable> void setValue(String name, T value);
    <T extends Serializable> Optional<T> getValue(String name, Class<T> tClass);
    void removeValue(String name);
    Set<Integer> participantIds();
    long getStudyId();
    Integer getStudyGroupId();
    // TODO
    /*
    * webhook(id, (params) -> ...)
    * pushNotify(participant, message)
    * kibanaRule(rule, hookId)
    * */
}
