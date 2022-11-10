package io.redlink.more.studymanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.redlink.more.studymanager.api.v1.model.ParticipantDTO;
import io.redlink.more.studymanager.controller.studymanager.ParticipantsApiV1Controller;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.service.ParticipantService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;

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

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("Create participant should create the participant with Id and status set")
    void testCreateParticipant() throws Exception {

        final long studyId = 1L;

        when(participantService.createParticipant(any(Participant.class))).thenAnswer(invocationOnMock -> new Participant()
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

        ParticipantDTO[] participantDTOS = new ParticipantDTO[] { participantRequest };

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
    @DisplayName("Update participant should return similar values")
    void testUpdateStudy() throws Exception {
        when(participantService.updateParticipant(any(Participant.class))).thenAnswer(invocationOnMock -> {
            return ((Participant)invocationOnMock.getArgument(0))
                    .setStatus(Participant.Status.NEW)
                    .setAlias("person x")
                    .setCreated(new Timestamp(0))
                    .setModified(new Timestamp(0));
        });

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