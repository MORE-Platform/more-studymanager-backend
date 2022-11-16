package io.redlink.more.studymanager.controller.studymanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.redlink.more.studymanager.api.v1.model.ObservationDTO;
import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.service.ObservationService;
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

@WebMvcTest({ObservationsApiV1Controller.class})
@AutoConfigureMockMvc(addFilters = false)
class ObservationControllerTest {

    @MockBean
    ObservationService observationService;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("Create Observation should create and then return the observation with observation id set")
    void testAddObservation() throws Exception {
        when(observationService.addObservation(any(Observation.class)))
                .thenAnswer(invocationOnMock -> new Observation()
                        .setStudyId(((Observation)invocationOnMock.getArgument(0)).getStudyId())
                        .setObservationId(((Observation)invocationOnMock.getArgument(0)).getObservationId())
                        .setTitle(((Observation)invocationOnMock.getArgument(0)).getTitle())
                        .setPurpose(((Observation)invocationOnMock.getArgument(0)).getPurpose())
                        .setParticipantInfo(((Observation)invocationOnMock.getArgument(0)).getParticipantInfo())
                        .setType(((Observation)invocationOnMock.getArgument(0)).getType())
                        .setProperties(((Observation)invocationOnMock.getArgument(0)).getProperties())
                        .setStudyGroupId(((Observation)invocationOnMock.getArgument(0)).getStudyGroupId())
                        .setCreated(new Timestamp(System.currentTimeMillis()))
                        .setModified(new Timestamp(System.currentTimeMillis())));

        ObservationDTO observationRequest = new ObservationDTO()
                .title("observation 1")
                .observationId(1)
                .studyId(1L)
                .purpose("some purpose")
                .participantInfo("info")
                .type("accelerometer")
                .properties("{\"name\":\"value\"}")
                .studyGroupId(1);

        mvc.perform(post("/api/v1/studies/1/observations")
                        .content(mapper.writeValueAsString(observationRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(observationRequest.getTitle()))
                .andExpect(jsonPath("$.observationId").value(observationRequest.getObservationId()))
                .andExpect(jsonPath("$.type").value(observationRequest.getType()))
                .andExpect(jsonPath("$.properties.name").value("value"));
    }

    @Test
    @DisplayName("Update observation should return similar values")
    void testUpdateStudy() throws Exception {
        when(observationService.updateObservation(any(Observation.class))).thenAnswer(invocationOnMock -> {
            return ((Observation)invocationOnMock.getArgument(0))
                    .setTitle("title")
                    .setCreated(new Timestamp(0))
                    .setModified(new Timestamp(0));
        });

        ObservationDTO observationRequest = new ObservationDTO()
                .studyId(1L)
                .title("a different title")
                .observationId(1);

        mvc.perform(put("/api/v1/studies/1/observations/1")
                        .content(mapper.writeValueAsString(observationRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("title"))
                .andExpect(jsonPath("$.studyId").value(observationRequest.getStudyId()))
                .andExpect(jsonPath("$.modified").exists())
                .andExpect(jsonPath("$.created").exists());
    }
}



