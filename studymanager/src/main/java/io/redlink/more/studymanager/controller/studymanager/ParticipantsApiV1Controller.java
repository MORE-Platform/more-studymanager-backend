package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.api.v1.model.ParticipantDTO;
import io.redlink.more.studymanager.api.v1.webservices.ParticipantsApi;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.transformer.ParticipantTransformer;
import io.redlink.more.studymanager.service.ParticipantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class ParticipantsApiV1Controller implements ParticipantsApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParticipantsApiV1Controller.class);
    private final ParticipantService service;

    public ParticipantsApiV1Controller(ParticipantService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<ParticipantDTO>> createParticipants(Long studyId, List<ParticipantDTO> participantDTO) {
        List<Participant> participants = participantDTO.stream().map(participant -> service.createParticipant(
                ParticipantTransformer.fromParticipantDTO_V1(participant.studyId(studyId))))
                .toList();
        LOGGER.debug("Participants created: {}", participants);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                participants.stream().map(ParticipantTransformer::toParticipantDTO_V1).toList()
        );
    }

    @Override
    public ResponseEntity<Void> deleteParticipant(Long studyId, Integer participantId) {
        service.deleteParticipant(studyId, participantId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ParticipantDTO> getParticipant(Long studyId, Integer participantId) {
        return ResponseEntity.ok(
                ParticipantTransformer.toParticipantDTO_V1(service.getParticipant(studyId, participantId))
        );
    }

    @Override
    public ResponseEntity<List<ParticipantDTO>> listParticipants(Long studyId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                service.listParticipants(studyId).stream().map(ParticipantTransformer::toParticipantDTO_V1).toList()
        );
    }

    @Override
    public ResponseEntity<ParticipantDTO> updateParticipant(Long studyId, Integer participantId, ParticipantDTO participantDTO) {
        Participant participant = service.updateParticipant(ParticipantTransformer.fromParticipantDTO_V1(participantDTO)
                .setParticipantId(participantId)
                .setStudyId(studyId));
        return ResponseEntity.status(HttpStatus.OK).body(
                ParticipantTransformer.toParticipantDTO_V1(participant)
        );
    }
}
