/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache License, Version 2.0.
 */
package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.ParticipantMilestoneDTO;
import io.redlink.more.studymanager.model.ParticipantMilestone;

public final class ParticipantMilestoneTransformer {

    private ParticipantMilestoneTransformer() {
    }

    public static ParticipantMilestoneDTO toParticipantMilestoneDTO_V1(ParticipantMilestone participantMilestone) {
        return new ParticipantMilestoneDTO()
                .participantMilestoneId(participantMilestone.getParticipantMilestoneId())
                .studyId(participantMilestone.getStudyId())
                .participantId(participantMilestone.getParticipantId())
                .milestoneId(participantMilestone.getMilestoneId())
                .name(participantMilestone.getName())
                .createdAt(participantMilestone.getCreated())
                .modifiedAt(participantMilestone.getModified())
                .dateTime(participantMilestone.getDateTime());
    }

}
