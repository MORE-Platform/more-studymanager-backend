package io.redlink.more.studymanager.core.sdk;

import java.io.Serializable;
import java.util.Optional;

public interface MorePlatformSDK {
    <T extends Serializable> void setValue(String name, T value);
    <T extends Serializable> Optional<T> getValue(String name, Class<T> tClass);
    void removeValue(String name);

    // TODO
    /*
    * webhook(id, (params) -> ...)
    * pushNotify(participant, message)
    * kibanaRule(rule, hookId)
    * */
}
