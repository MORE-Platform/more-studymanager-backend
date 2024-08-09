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

public class ParticipantTransformer {

    private ParticipantTransformer() {

    }

    public static Participant fromParticipantDTO_V1(ParticipantDTO participantDTO) {
        return new Participant()
                .setStudyId(participantDTO.getStudyId())
                .setParticipantId(participantDTO.getParticipantId())
                .setAlias(participantDTO.getAlias())
                .setStudyGroupId(participantDTO.getStudyGroupId());
    }

    public static ParticipantDTO toParticipantDTO_V1(Participant participant) {
        return new ParticipantDTO()
                .studyId(participant.getStudyId())
                .participantId(participant.getParticipantId())
                .alias(participant.getAlias())
                .studyGroupId(participant.getStudyGroupId())
                .registrationToken(participant.getRegistrationToken())
                .status(ParticipantStatusDTO.fromValue(participant.getStatus().getValue()))
                .start(Transformers.toOffsetDateTime(participant.getStart()))
                .modified(Transformers.toOffsetDateTime(participant.getModified()))
                .created(Transformers.toOffsetDateTime(participant.getCreated()));
    }

}
