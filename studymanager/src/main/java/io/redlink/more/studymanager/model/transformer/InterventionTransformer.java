package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.InterventionDTO;
import io.redlink.more.studymanager.model.Intervention;

public class InterventionTransformer {

    public static Intervention fromInterventionDTO_V1(InterventionDTO dto) {
        return new Intervention();
    }

    public static InterventionDTO toInterventionDTO_V1(Intervention intervention) {
        return new InterventionDTO();
    }

}
