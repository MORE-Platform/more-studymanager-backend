package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.ActionDTO;
import io.redlink.more.studymanager.core.properties.ActionProperties;
import io.redlink.more.studymanager.model.Action;
import io.redlink.more.studymanager.utils.MapperUtils;

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
        return new ActionDTO()
                .actionId(action.getActionId())
                .type(action.getType())
                .properties(action.getProperties())
                .created(Transformers.toOffsetDateTime(action.getCreated()))
                .modified(Transformers.toOffsetDateTime(action.getModified()));
    }

}
