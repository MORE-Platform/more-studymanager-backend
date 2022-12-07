package io.redlink.more.studymanager.controller.studymanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.redlink.more.studymanager.api.v1.model.ParticipantDTO;
import io.redlink.more.studymanager.model.AuthenticatedUser;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.PlatformRole;
import io.redlink.more.studymanager.model.User;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import io.redlink.more.studymanager.service.ParticipantService;
import java.sql.Timestamp;
import java.util.EnumSet;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ParticipantsApiV1Controller.class})
@AutoConfigureMockMvc(addFilters = false)
class ParticipantControllerTest {

    @MockBean
    ParticipantService participantService;
    @MockBean
    OAuth2AuthenticationService oAuth2AuthenticationService;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        when(oAuth2AuthenticationService.getCurrentUser()).thenReturn(
                new AuthenticatedUser(
                        UUID.randomUUID().toString(),
                        "Test User", "test@example.com", "Test Inc.",
                        EnumSet.allOf(PlatformRole.class)
                )
        );
    }

    @Test
    @DisplayName("Create participant should create the participant with Id and status set")
    void testCreateParticipant() throws Exception {

        final long studyId = 1L;

        when(participantService.createParticipant(any(Participant.class), any(User.class))).thenAnswer(invocationOnMock -> new Participant()
                .setStudyId(1L)
                .setParticipantId(1)
                .setAlias("participant x")
                .setStudyGroupId(1)
                .setStatus(Participant.Status.NEW)
                .setCreated(new Timestamp(System.currentTimeMillis()))
                .setModified(new Timestamp(System.currentTimeMillis()))
                .setRegistrationToken("TEST123"));

        ParticipantDTO participantRequest = new ParticipantDTO()
                .studyId(studyId)
                .alias("participant x")
                .studyGroupId(1);

        ParticipantDTO[] participantDTOS = new ParticipantDTO[]{participantRequest};

        mvc.perform(post("/api/v1/studies/{studyId}/participants", studyId)
                        .content(mapper.writeValueAsString(participantDTOS))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].studyId").value(participantRequest.getStudyId()))
                .andExpect(jsonPath("$[0].participantId").value(1))
                .andExpect(jsonPath("$[0].status").value("new"))
                .andExpect(jsonPath("$[0].studyGroupId").value(participantRequest.getStudyGroupId()))
                .andExpect(jsonPath("$[0].registrationToken").exists());
    }

    @Test
    @DisplayName("Participants can be created from csv")
    void testCreateFromCSV() throws Exception {
        String participant1 = "Participant1";
        String participant2 = "Participant2";
        when(participantService.createParticipant(any(Participant.class), any(User.class))).thenAnswer(invocationOnMock -> new Participant()
                        .setStudyId(1L)
                        .setParticipantId(1)
                        .setAlias(participant1)
                        .setStudyGroupId(1)
                        .setStatus(Participant.Status.NEW)
                        .setCreated(new Timestamp(System.currentTimeMillis()))
                        .setModified(new Timestamp(System.currentTimeMillis()))
                        .setRegistrationToken("TEST123"))
                .thenAnswer(invocationOnMock -> new Participant()
                        .setStudyId(1L)
                        .setParticipantId(1)
                        .setAlias(participant2)
                        .setStudyGroupId(1)
                        .setStatus(Participant.Status.NEW)
                        .setCreated(new Timestamp(System.currentTimeMillis()))
                        .setModified(new Timestamp(System.currentTimeMillis()))
                        .setRegistrationToken("TEST123"));

        String csv = "Participants\n%s\n%s".formatted(participant1, participant2);
        mvc.perform(post("/api/v1/studies/{studyId}/participants", 1L)
                        .content(csv)
                        .contentType(new MediaType("text", "csv")))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Update participant should return similar values")
    void testUpdateStudy() throws Exception {
        when(participantService.updateParticipant(any(Participant.class), any(User.class))).thenAnswer(invocationOnMock ->
                invocationOnMock.getArgument(0, Participant.class)
                        .setStatus(Participant.Status.NEW)
                        .setAlias("person x")
                        .setCreated(new Timestamp(0))
                        .setModified(new Timestamp(0)));

        ParticipantDTO participantRequest = new ParticipantDTO()
                .studyId(1L)
                .participantId(1)
                .studyGroupId(1)
                .alias("person x");

        mvc.perform(put("/api/v1/studies/1/participants/1")
                        .content(mapper.writeValueAsString(participantRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studyId").value(participantRequest.getStudyId()))
                .andExpect(jsonPath("$.participantId").value(participantRequest.getParticipantId()))
                .andExpect(jsonPath("$.studyGroupId").value(participantRequest.getStudyGroupId()))
                .andExpect(jsonPath("$.alias").value(participantRequest.getAlias()))
                .andExpect(jsonPath("$.modified").exists())
                .andExpect(jsonPath("$.created").exists());
    }
}
