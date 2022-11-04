package io.redlink.more.studymanager.core.sdk;

import java.util.Optional;

public interface MorePlatformSDK {
    <T> void setValue(String name, T value);
    <T> Optional<T> getValue(String name, Class<T> tClass);
    void removeValue(String name);

    // TODO
    /*
    * webhook(id, (params) -> ...)
    * pushNotify(participant, message)
    * kibanaRule(rule, hookId)
    * */
}
