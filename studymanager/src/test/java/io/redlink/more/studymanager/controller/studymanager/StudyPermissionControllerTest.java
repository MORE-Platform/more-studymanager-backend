package io.redlink.more.studymanager.controller.studymanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.redlink.more.studymanager.model.*;
import io.redlink.more.studymanager.repository.UserRepository;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import io.redlink.more.studymanager.service.StudyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.*;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@DirtiesContext
@ActiveProfiles("test-containers-flyway")
class StudyPermissionControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    StudyService studyService;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    OAuth2AuthenticationService authService;

    private MockMvc mvc;

    private final AuthenticatedUser user1 = new AuthenticatedUser(
            "collaborator1",
            "More test User 1",
            "more@example.com",
            "The Hospital",
            EnumSet.allOf(PlatformRole.class));

    private final AuthenticatedUser user2 = new AuthenticatedUser(
            "collaborator2",
            "More test User 2",
            "more@example.com",
            "The Hospital",
            EnumSet.allOf(PlatformRole.class));

    @BeforeEach
    public void init(){
        AuthenticatedUser studyCreator = new AuthenticatedUser(
                UUID.randomUUID().toString(),
                "More Study creator",
                "more@example.com",
                "The Hospital",
                EnumSet.allOf(PlatformRole.class));

        studyService.createStudy(new Study()
                        .setTitle("testRoleAuth")
                        .setStudyId(1L)
                        .setPlannedStartDate(new Date(System.currentTimeMillis()))
                        .setPlannedEndDate(new Date(System.currentTimeMillis()))
                        .setStudyState(Study.Status.DRAFT)
                        .setCreated(new Timestamp(System.currentTimeMillis()))
                        .setModified(new Timestamp(System.currentTimeMillis())),
                studyCreator);

        userRepository.save(user1);
        userRepository.save(user2);
        studyService.setRolesForStudy(1L, user1.id(), new HashSet<>(){{add(StudyRole.STUDY_VIEWER);}}, studyCreator);
    }
    @BeforeEach
    public void mvcSetup(){
        this.mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser
    void testRoleChecksDenied() throws Exception {
        when(authService.getCurrentUser()).thenReturn(user2);
        mvc.perform(get("/api/v1/studies/1/collaborators")
                        .content("testContent"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void testRoleChecksAccepted() throws Exception{
        when(authService.getCurrentUser()).thenReturn(user1);
        mvc.perform(get("/api/v1/studies/1/collaborators")
                        .content("testContent"))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
