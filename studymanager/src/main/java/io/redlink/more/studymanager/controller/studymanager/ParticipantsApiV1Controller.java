package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.api.v1.model.ParticipantDTO;
import io.redlink.more.studymanager.api.v1.webservices.ParticipantsApi;
import io.redlink.more.studymanager.controller.RequiresStudyRole;
import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.model.transformer.ParticipantTransformer;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import io.redlink.more.studymanager.service.ParticipantService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class ParticipantsApiV1Controller implements ParticipantsApi {
    private final ParticipantService service;

    private final OAuth2AuthenticationService authService;


    public ParticipantsApiV1Controller(ParticipantService service, OAuth2AuthenticationService authService) {
        this.service = service;
        this.authService = authService;
    }

    @Override
    public ResponseEntity<List<ParticipantDTO>> createParticipants(Long studyId, List<ParticipantDTO> participantDTO) {
        final var currentUser = authService.getCurrentUser();
        List<Participant> participants = participantDTO.stream()
                .map(p -> p.studyId(studyId))
                .map(ParticipantTransformer::fromParticipantDTO_V1)
                .map(participant -> service.createParticipant(participant, currentUser))
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(
                participants.stream()
                        .map(ParticipantTransformer::toParticipantDTO_V1)
                        .toList()
        );
    }

    @Override
    public ResponseEntity<List<ParticipantDTO>> updateParticipantList(Long studyId, List<ParticipantDTO> participantDTO) {
        final var currentUser = authService.getCurrentUser();
        if (participantDTO.stream().anyMatch(p -> p.getParticipantId() == null)) {
            throw new NotFoundException("Participant without id");
        }
        List<Participant> participants = participantDTO.stream()
                .map(p -> p.studyId(studyId))
                .map(ParticipantTransformer::fromParticipantDTO_V1)
                .map(participant -> service.updateParticipant(participant, currentUser))
                .toList();
        return ResponseEntity.ok(
                participants.stream()
                        .map(ParticipantTransformer::toParticipantDTO_V1)
                        .toList()
        );
    }

    @Override
    public ResponseEntity<ParticipantDTO> deleteParticipant(Long studyId, Integer participantId) {
        final var currentUser = authService.getCurrentUser();
        return service.deleteParticipant(studyId, participantId, currentUser)
                .map(ParticipantTransformer::toParticipantDTO_V1)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @Override
    public ResponseEntity<ParticipantDTO> getParticipant(Long studyId, Integer participantId) {
        final var currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(
                ParticipantTransformer.toParticipantDTO_V1(service.getParticipant(studyId, participantId, currentUser))
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<List<ParticipantDTO>> listParticipants(Long studyId) {
        final var currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(
                service.listParticipants(studyId, currentUser).stream()
                        .map(ParticipantTransformer::toParticipantDTO_V1)
                        .toList()
        );
    }

    @Override
    public ResponseEntity<ParticipantDTO> updateParticipant(Long studyId, Integer participantId, ParticipantDTO participantDTO) {
        final var currentUser = authService.getCurrentUser();
        Participant participant = service.updateParticipant(
                ParticipantTransformer.fromParticipantDTO_V1((participantDTO.studyId(studyId)).participantId(participantId)),
                currentUser
        );
        return ResponseEntity.status(HttpStatus.OK).body(
                ParticipantTransformer.toParticipantDTO_V1(participant)
        );
    }
}
