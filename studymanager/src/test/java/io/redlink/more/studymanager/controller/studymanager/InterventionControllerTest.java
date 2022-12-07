package io.redlink.more.studymanager.controller.studymanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.redlink.more.studymanager.api.v1.model.ActionDTO;
import io.redlink.more.studymanager.api.v1.model.EventDTO;
import io.redlink.more.studymanager.api.v1.model.InterventionDTO;
import io.redlink.more.studymanager.api.v1.model.TriggerDTO;
import io.redlink.more.studymanager.core.properties.TriggerProperties;
import io.redlink.more.studymanager.model.Action;
import io.redlink.more.studymanager.model.AuthenticatedUser;
import io.redlink.more.studymanager.model.Event;
import io.redlink.more.studymanager.model.Intervention;
import io.redlink.more.studymanager.model.PlatformRole;
import io.redlink.more.studymanager.model.Trigger;
import io.redlink.more.studymanager.model.User;
import io.redlink.more.studymanager.service.InterventionService;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import io.redlink.more.studymanager.utils.MapperUtils;
import java.util.EnumSet;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({InterventionsApiV1Controller.class})
@AutoConfigureMockMvc(addFilters = false)
class InterventionControllerTest {

    @MockBean
    InterventionService interventionService;

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
    @DisplayName("Create Intervention should create and then return the intervention with intervention id set")
    void testAddIntervention() throws Exception {

        Instant dateStart = Instant.now();
        Instant dateEnd = dateStart.plus(2, ChronoUnit.HOURS);

        when(interventionService.addIntervention(any(Intervention.class), any(User.class)))
                .thenAnswer(invocationOnMock -> new Intervention()
                        .setStudyId(((Intervention)invocationOnMock.getArgument(0)).getStudyId())
                        .setInterventionId(((Intervention)invocationOnMock.getArgument(0)).getInterventionId())
                        .setTitle(((Intervention)invocationOnMock.getArgument(0)).getTitle())
                        .setPurpose(((Intervention)invocationOnMock.getArgument(0)).getPurpose())
                        .setStudyGroupId(((Intervention)invocationOnMock.getArgument(0)).getStudyGroupId())
                        .setSchedule(new Event()
                                .setDateStart(dateStart)
                                .setDateEnd(dateEnd))
                        .setCreated(Instant.now())
                        .setModified(Instant.now()));

        InterventionDTO interventionRequest = new InterventionDTO()
                .title("intervention 1")
                .interventionId(1)
                .studyId(1L)
                .purpose("some purpose")
                .studyGroupId(1)
                .schedule(new EventDTO()
                        .dtstart(dateStart.atOffset(ZoneOffset.UTC))
                        .dtend(dateEnd.atOffset(ZoneOffset.UTC)));

        mvc.perform(post("/api/v1/studies/1/interventions")
                        .content(mapper.writeValueAsString(interventionRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(interventionRequest.getTitle()))
                .andExpect(jsonPath("$.interventionId").value(interventionRequest.getInterventionId()))
                .andExpect(jsonPath("$.schedule").exists());
    }

    @Test
    @DisplayName("Update intervention should return similar values")
    void testUpdateIntervention() throws Exception {

        Instant dateStart = Instant.now();
        Instant dateEnd = dateStart.plus(2, ChronoUnit.HOURS);

        when(interventionService.updateIntervention(any(Intervention.class), any(User.class))).thenAnswer(invocationOnMock ->
                ((Intervention)invocationOnMock.getArgument(0))
                .setStudyId(1L)
                .setInterventionId(1)
                .setStudyGroupId(1)
                .setPurpose("some updated purpose")
                .setTitle("a title")
                .setSchedule(new Event()
                        .setDateStart(dateStart)
                        .setDateEnd(dateEnd))
                .setCreated(Instant.now())
                .setModified(Instant.now()));

        InterventionDTO interventionRequest = new InterventionDTO()
                .studyId(1L)
                .interventionId(1)
                .studyGroupId(1)
                .purpose("some purpose")
                .title("a title")
                .schedule(new EventDTO()
                        .dtstart(dateStart.atOffset(ZoneOffset.UTC))
                        .dtend(dateEnd.atOffset(ZoneOffset.UTC)));

        mvc.perform(put("/api/v1/studies/1/interventions/1")
                        .content(mapper.writeValueAsString(interventionRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studyId").value(interventionRequest.getStudyId()))
                .andExpect(jsonPath("$.interventionId").value(interventionRequest.getInterventionId()))
                .andExpect(jsonPath("$.studyGroupId").value(interventionRequest.getStudyGroupId()))
                .andExpect(jsonPath("$.title").value(interventionRequest.getTitle()))
                .andExpect(jsonPath("$.purpose").value("some updated purpose"))
                .andExpect(jsonPath("$.schedule").exists())
                .andExpect(jsonPath("$.modified").exists())
                .andExpect(jsonPath("$.created").exists());
    }

    @Test
    @DisplayName("A trigger can be updated")
    void testUpdateAndGetTrigger() throws Exception {
        when(interventionService.updateTrigger(any(Long.class), any(Integer.class), any(Trigger.class), any(User.class)))
                .thenAnswer(invocationOnMock -> ((Trigger)invocationOnMock.getArgument(2))
                .setType("my-type")
                .setProperties(MapperUtils.MAPPER.convertValue(Map.of("name", "value"), TriggerProperties.class))
                .setCreated(Instant.now())
                .setModified(Instant.now()));

        TriggerDTO triggerRequest = new TriggerDTO()
                .properties(Map.of("name", "value"))
                .type("my-type");

        mvc.perform(put("/api/v1/studies/1/interventions/1/trigger")
                        .content(MapperUtils.MAPPER.writeValueAsString(triggerRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value(triggerRequest.getType()))
                .andExpect(jsonPath("$.properties.name").value("value"));
    }

    @Test
    @DisplayName("Creating an Action should return the Action with Id and timestamps set")
    @Disabled("Do not get the test-idea")
    void testPostAndPutOfAction() throws Exception {
        when(interventionService.createAction(any(Long.class), any(Integer.class), any(Action.class), any(User.class)))
                .thenAnswer(invocationOnMock -> new Action()
                        .setActionId(((Action)invocationOnMock.getArgument(2)).getActionId())
                        .setType(((Action)invocationOnMock.getArgument(2)).getType())
                        .setProperties(((Action) invocationOnMock.getArgument(2)).getProperties())
                        .setCreated(Instant.now())
                        .setModified(Instant.now()));

        ActionDTO actionRequest = new ActionDTO()
                .actionId(1)
                .type("my-type")
                .properties(Map.of("property", "value"));

        // no type for action yet, bad request
        mvc.perform(post("/api/v1/studies/1/interventions/1/actions")
                        .content(mapper.writeValueAsString(actionRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""))
                .andReturn();

    }
}


