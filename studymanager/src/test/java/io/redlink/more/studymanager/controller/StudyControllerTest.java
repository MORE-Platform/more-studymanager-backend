package io.redlink.more.studymanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.redlink.more.studymanager.api.v1.model.StudyDTO;
import io.redlink.more.studymanager.api.v1.model.StudyStatusDTO;
import io.redlink.more.studymanager.controller.studymanager.StudyApiV1Controller;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.service.StudyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({StudyApiV1Controller.class})
@AutoConfigureMockMvc(addFilters = false)
class StudyControllerTest {

    @MockBean
    StudyService studyService;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("Create study should create and then return the study with id and status set.")
    void testCreateStudy() throws Exception {
        when(studyService.createStudy(any(Study.class))).thenAnswer(invocationOnMock -> new Study()
                .setTitle(((Study)invocationOnMock.getArgument(0)).getTitle())
                .setStudyId(1L)
                .setPlannedStartDate(((Study)invocationOnMock.getArgument(0)).getPlannedStartDate())
                .setPlannedEndDate(((Study)invocationOnMock.getArgument(0)).getPlannedEndDate())
                .setStudyState(Study.Status.DRAFT)
                .setCreated(new Timestamp(System.currentTimeMillis()))
                .setModified(new Timestamp(System.currentTimeMillis())));

        StudyDTO studyRequest = new StudyDTO()
                .title("Some title")
                .plannedStart(LocalDate.now())
                .plannedEnd(LocalDate.now());

        mvc.perform(post("/api/v1/studies")
                .content(mapper.writeValueAsString(studyRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(studyRequest.getTitle()))
                .andExpect(jsonPath("$.studyId").value(1L))
                .andExpect(jsonPath("$.status").value("draft"));
    }

    @Test
    @DisplayName("Update study should return similar values")
    void testUpdateStudy() throws Exception {
        when(studyService.updateStudy(any(Study.class))).thenAnswer(invocationOnMock -> {
            return ((Study)invocationOnMock.getArgument(0))
                    .setStudyState(Study.Status.DRAFT)
                    .setCreated(new Timestamp(0))
                    .setModified(new Timestamp(0));
        });

        StudyDTO studyRequest = new StudyDTO()
                .studyId(1L)
                .title("t")
                .purpose("p")
                .consentInfo("ci")
                .participantInfo("pi")
                .plannedStart(LocalDate.now())
                .plannedEnd(LocalDate.now());

        mvc.perform(put("/api/v1/studies/1")
                        .content(mapper.writeValueAsString(studyRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(studyRequest.getTitle()))
                .andExpect(jsonPath("$.studyId").value(studyRequest.getStudyId()))
                .andExpect(jsonPath("$.purpose").value(studyRequest.getPurpose()))
                .andExpect(jsonPath("$.consentInfo").value(studyRequest.getConsentInfo()))
                .andExpect(jsonPath("$.participantInfo").value(studyRequest.getParticipantInfo()))
                .andExpect(jsonPath("$.plannedStart").exists())
                .andExpect(jsonPath("$.plannedEnd").exists())
                .andExpect(jsonPath("$.modified").exists())
                .andExpect(jsonPath("$.created").exists());
    }
}
