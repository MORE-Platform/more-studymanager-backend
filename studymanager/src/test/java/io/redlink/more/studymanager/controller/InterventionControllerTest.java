package io.redlink.more.studymanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.redlink.more.studymanager.api.v1.model.InterventionDTO;
import io.redlink.more.studymanager.controller.studymanager.InterventionsApiV1Controller;
import io.redlink.more.studymanager.model.Intervention;
import io.redlink.more.studymanager.service.InterventionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({InterventionsApiV1Controller.class})
@AutoConfigureMockMvc(addFilters = false)
class InterventionControllerTest {

    @MockBean
    InterventionService interventionService;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("Create Intervention should create and then return the intervention with intervention id set")
    void testAddIntervention() throws Exception {
        when(interventionService.addIntervention(any(Intervention.class)))
                .thenAnswer(invocationOnMock -> new Intervention()
                        .setStudyId(((Intervention)invocationOnMock.getArgument(0)).getStudyId())
                        .setInterventionId(((Intervention)invocationOnMock.getArgument(0)).getInterventionId())
                        .setTitle(((Intervention)invocationOnMock.getArgument(0)).getTitle())
                        .setPurpose(((Intervention)invocationOnMock.getArgument(0)).getPurpose())
                        .setStudyGroupId(((Intervention)invocationOnMock.getArgument(0)).getStudyGroupId())
                        .setCreated(Instant.now())
                        .setModified(Instant.now()));

        InterventionDTO interventionRequest = new InterventionDTO()
                .title("intervention 1")
                .interventionId(1)
                .studyId(1L)
                .purpose("some purpose")
                .studyGroupId(1);

        mvc.perform(post("/api/v1/studies/1/interventions")
                        .content(mapper.writeValueAsString(interventionRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(interventionRequest.getTitle()))
                .andExpect(jsonPath("$.interventionId").value(interventionRequest.getInterventionId()))
                .andExpect(jsonPath("$.schedule").value(interventionRequest.getSchedule()));
    }
}


