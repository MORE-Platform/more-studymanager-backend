package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.model.*;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import io.redlink.more.studymanager.service.StudyPermissionService;
import io.redlink.more.studymanager.service.StudyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test-containers-flyway")
class CollaboratorsControllerTest {
    @MockBean
    StudyPermissionService studyPermissionService;

    @MockBean
    StudyService studyService;

    @MockBean
    OAuth2AuthenticationService authService;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;
    @BeforeEach
    public void setup(){
        this.mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    private final AuthenticatedUser authUser = new AuthenticatedUser(
            UUID.randomUUID().toString(),
            "More User",
            "more@example.com",
            "The Hospital",
            EnumSet.allOf(PlatformRole.class));

    @WithMockUser
    @Test
    void testRoleChecksDenied() throws Exception {
        when(authService.getCurrentUser()).thenReturn(authUser);
        when(studyPermissionService.hasAnyRole(anyLong(), anyString(), anySet())).thenReturn(false);

        mvc.perform(get("/api/v1/studies/2/collaborators")
                        .content("testContent"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @WithMockUser
    @Test
    void testRoleChecksAccepted() throws Exception{
        when(authService.getCurrentUser()).thenReturn(authUser);
        when(studyPermissionService.hasAnyRole(anyLong(), anyString(), anySet())).thenReturn(true);
        when(studyService.getACL(anyLong(),any(User.class)))
                .thenReturn(Map.of(new MoreUser("test","test","test","test"), Set.of(StudyRole.STUDY_ADMIN)));

        mvc.perform(get("/api/v1/studies/2/collaborators")
                        .content("testContent"))
                .andDo(print())
                .andExpect(status().isOk());

    }
}
