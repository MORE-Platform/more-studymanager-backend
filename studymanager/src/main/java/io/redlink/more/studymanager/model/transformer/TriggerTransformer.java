/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.TriggerDTO;
import io.redlink.more.studymanager.core.properties.TriggerProperties;
import io.redlink.more.studymanager.model.Trigger;
import io.redlink.more.studymanager.utils.MapperUtils;
import java.time.Instant;

public final class TriggerTransformer {

    private TriggerTransformer() {
    }

    public static Trigger fromTriggerDTO_V1(TriggerDTO dto) {
        return new Trigger()
                .setType(dto.getType())
                .setProperties(MapperUtils.MAPPER.convertValue(dto.getProperties(), TriggerProperties.class));
    }

    public static TriggerDTO toTriggerDTO_V1(Trigger trigger) {
        Instant instant = trigger.getModified();
        Instant instant1 = trigger.getCreated();
        return new TriggerDTO()
                .type(trigger.getType())
                .properties(trigger.getProperties())
                .created(instant1)
                .modified(instant);
    }
}
