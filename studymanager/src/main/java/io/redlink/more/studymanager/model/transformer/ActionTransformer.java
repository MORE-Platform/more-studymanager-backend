/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.ActionDTO;
import io.redlink.more.studymanager.core.properties.ActionProperties;
import io.redlink.more.studymanager.model.Action;
import io.redlink.more.studymanager.utils.MapperUtils;
import java.time.Instant;

public final class ActionTransformer {

    private ActionTransformer() {
    }

    public static Action fromActionDTO_V1(ActionDTO dto) {
        return new Action()
                .setActionId(dto.getActionId())
                .setType(dto.getType())
                .setProperties(MapperUtils.readObject(dto.getProperties(), ActionProperties.class));
    }

    public static ActionDTO toActionDTO_V1(Action action) {
        Instant instant = action.getModified();
        Instant instant1 = action.getCreated();
        return new ActionDTO()
                .actionId(action.getActionId())
                .type(action.getType())
                .properties(action.getProperties())
                .created(instant1)
                .modified(instant);
    }

}
