/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.model.MoreUser;
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.service.StudyPermissionService;
import io.redlink.more.studymanager.service.StudyService;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@DirtiesContext
@ActiveProfiles("test-containers-flyway")
class StudyPermissionControllerTest {

    @MockitoBean
    StudyService studyService;

    @MockitoBean
    StudyPermissionService studyPermissionService;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void init() {
        this.mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser
    void testRoleChecksDenied() throws Exception {
        when(studyPermissionService.hasAnyRole(anyLong(),anyString(),anySet())).thenReturn(false);
        mvc.perform(get("/api/v1/studies/1/collaborators")
                        .content("testContent"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void testRoleChecksAccepted() throws Exception {
        when(studyPermissionService.hasAnyRole(anyLong(),anyString(),anySet())).thenReturn(true);
        when(studyService.getACL(anyLong())).thenReturn(Map.of(new MoreUser("test","test","test","test"), Set.of(StudyRole.STUDY_VIEWER)));
        mvc.perform(get("/api/v1/studies/1/collaborators")
                        .content("testContent"))
                .andDo(print())
                .andExpect(status().isOk());
    }
}