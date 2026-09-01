/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache License, Version 2.0.
 */
package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.MilestoneDTO;
import io.redlink.more.studymanager.model.Milestone;

public final class MilestoneTransformer {

    private MilestoneTransformer() {
    }

    public static MilestoneDTO toMilestoneDTO_V1(Milestone milestone) {
        return new MilestoneDTO()
                .milestoneId(milestone.getMilestoneId())
                .studyId(milestone.getStudyId())
                .createdAt(milestone.getCreated())
                .name(milestone.getName())
                .orderIndex(milestone.getOrderIndex());
    }

}
