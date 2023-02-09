package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.TriggerDTO;
import io.redlink.more.studymanager.core.properties.TriggerProperties;
import io.redlink.more.studymanager.model.Trigger;
import io.redlink.more.studymanager.utils.MapperUtils;

public final class TriggerTransformer {

    private TriggerTransformer() {
    }

    public static Trigger fromTriggerDTO_V1(TriggerDTO dto) {
        return new Trigger()
                .setType(dto.getType())
                .setProperties(MapperUtils.MAPPER.convertValue(dto.getProperties(), TriggerProperties.class));
    }

    public static TriggerDTO toTriggerDTO_V1(Trigger trigger) {
        return new TriggerDTO()
                .type(trigger.getType())
                .properties(trigger.getProperties())
                .created(Transformers.toOffsetDateTime(trigger.getCreated()))
                .modified(Transformers.toOffsetDateTime(trigger.getModified()));
    }
}
