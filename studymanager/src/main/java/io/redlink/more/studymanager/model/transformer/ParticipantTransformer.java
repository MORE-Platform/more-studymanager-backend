/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.ParticipantDTO;
import io.redlink.more.studymanager.api.v1.model.ParticipantStatusDTO;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.utils.ParticipantUtils;

import java.time.Instant;

public final class ParticipantTransformer {
    private ParticipantTransformer() { }

    public static Participant fromParticipantDTO_V1(ParticipantDTO participantDTO) {
        return new Participant()
                .setStudyId(participantDTO.getStudyId())
                .setParticipantId(participantDTO.getParticipantId())
                .setAlias(participantDTO.getAlias())
                .setStudyGroupId(participantDTO.getStudyGroupId());
    }

    public static ParticipantDTO toParticipantDTO_V1(Participant participant) {
        Instant instant = participant.getCreated();
        Instant instant1 = participant.getModified();
        Instant instant2 = participant.getStart();
        String registrationUrl = ParticipantUtils.getRegistrationUri(participant.getRegistrationToken());

        return new ParticipantDTO()
                .studyId(participant.getStudyId())
                .participantId(participant.getParticipantId())
                .alias(participant.getAlias())
                .studyGroupId(participant.getStudyGroupId())
                .registrationToken(participant.getRegistrationToken())
                .registrationUrl(registrationUrl)
                .status(ParticipantStatusDTO.fromValue(participant.getStatus().getValue()))
                .start(instant2)
                .modified(instant1)
                .created(instant);
    }
}
