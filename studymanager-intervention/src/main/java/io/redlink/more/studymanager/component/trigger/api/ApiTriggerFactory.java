package io.redlink.more.studymanager.component.trigger.api;

import java.util.List;

import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.TriggerFactory;
import io.redlink.more.studymanager.core.properties.TriggerProperties;
import io.redlink.more.studymanager.core.properties.model.Value;
import io.redlink.more.studymanager.core.sdk.MoreTriggerSDK;

public class ApiTriggerFactory extends TriggerFactory<ApiTrigger,TriggerProperties>{
    private static List<Value> properties = List.of(
        
    );
    @Override
    public String getId(){
        return "api-trigger";
    }

    @Override
    public String getTitle() {
        return "Api trigger intervention";
    }

    @Override
    public String getDescription() {
        return "Intervention triggered by external api";
    }

    @Override
    public List<Value> getProperties() {
        return properties;
    }

    @Override
    public ApiTrigger create(MoreTriggerSDK sdk, TriggerProperties properties) throws ConfigurationValidationException {
        return new ApiTrigger(sdk, properties);
    }
}
