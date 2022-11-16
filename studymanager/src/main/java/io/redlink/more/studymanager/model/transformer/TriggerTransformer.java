package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.TriggerDTO;
import io.redlink.more.studymanager.core.properties.TriggerProperties;
import io.redlink.more.studymanager.model.Trigger;
import io.redlink.more.studymanager.utils.MapperUtils;

import java.time.ZoneOffset;

public class TriggerTransformer {
    public static Trigger fromTriggerDTO_V1(TriggerDTO dto) {
        return new Trigger()
                .setType(dto.getType())
                .setProperties(MapperUtils.MAPPER.convertValue(dto.getProperties(), TriggerProperties.class));
    }

    public static TriggerDTO toTriggerDTO_V1(Trigger trigger) {
        return new TriggerDTO()
                .type(trigger.getType())
                .properties(trigger.getProperties())
                .created(trigger.getCreated().atOffset(ZoneOffset.UTC))
                .modified(trigger.getModified().atOffset(ZoneOffset.UTC));
    }
}
