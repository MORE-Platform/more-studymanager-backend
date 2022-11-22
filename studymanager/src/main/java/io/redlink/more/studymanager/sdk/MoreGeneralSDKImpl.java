package io.redlink.more.studymanager.sdk;

import io.redlink.more.studymanager.repository.NameValuePairRepository;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Optional;

@Component
public class MoreGeneralSDKImpl {

    public final NameValuePairRepository nvpairs;

    public MoreGeneralSDKImpl(NameValuePairRepository nvpairs) {
        this.nvpairs = nvpairs;
    }

    <T extends Serializable> void setValue(String issuer, String name, T value) {
        nvpairs.setValue(issuer, name, value);
    }

    <T extends Serializable> Optional<T> getValue(String issuer, String name, Class<T> tClass) {
        return nvpairs.getValue(issuer, name, tClass);
    }

    void removeValue(String issuer, String name) {
        nvpairs.removeValue(issuer, name);
    }
}
