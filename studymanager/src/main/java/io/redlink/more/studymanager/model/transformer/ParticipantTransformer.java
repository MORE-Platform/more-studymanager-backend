package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.ParticipantDTO;
import io.redlink.more.studymanager.api.v1.model.ParticipantStatusDTO;
import io.redlink.more.studymanager.model.Participant;

import java.time.OffsetDateTime;

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
                .modified(participant.getModified().toLocalDateTime().atOffset(OffsetDateTime.now().getOffset()))
                .created(participant.getCreated().toLocalDateTime().atOffset(OffsetDateTime.now().getOffset()));
    }

}
