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
import io.redlink.more.studymanager.properties.GatewayProperties;

import java.net.URI;
import java.time.Instant;

public final class ParticipantTransformer {
    private ParticipantTransformer() {
    }

    public static Participant fromParticipantDTO_V1(ParticipantDTO participantDTO) {
        return new Participant()
                .setStudyId(participantDTO.getStudyId())
                .setParticipantId(participantDTO.getParticipantId())
                .setAlias(participantDTO.getAlias())
                .setStudyGroupId(participantDTO.getStudyGroupId());
    }

    public static ParticipantDTO toParticipantDTO_V1(Participant participant, GatewayProperties gatewayProps) {
        Instant instant = participant.getCreated();
        Instant instant1 = participant.getModified();
        Instant instant2 = participant.getStart();
        URI regustrationUri = gatewayProps.generateSignupUrl(participant);

        return new ParticipantDTO()
                .studyId(participant.getStudyId())
                .participantId(participant.getParticipantId())
                .alias(participant.getAlias())
                .studyGroupId(participant.getStudyGroupId())
                .registrationToken(participant.getRegistrationToken())
                .registrationUrl(regustrationUri)
                .status(ParticipantStatusDTO.fromValue(participant.getStatus().getValue()))
                .start(instant2)
                .modified(instant1)
                .created(instant);
    }
}
