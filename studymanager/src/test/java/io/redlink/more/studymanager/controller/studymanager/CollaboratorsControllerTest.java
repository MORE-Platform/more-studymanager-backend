package io.redlink.more.studymanager.controller.studymanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.redlink.more.studymanager.api.v1.model.CollaboratorDTO;
import io.redlink.more.studymanager.api.v1.model.UserInfoDTO;
import io.redlink.more.studymanager.controller.security.ControllerPermissionSecurity;
import io.redlink.more.studymanager.model.*;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import io.redlink.more.studymanager.service.StudyPermissionService;
import io.redlink.more.studymanager.service.StudyService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({CollaboratorsApiV1Controller.class})
@AutoConfigureMockMvc(addFilters = false)
class CollaboratorsControllerTest {

    @MockBean
    StudyPermissionService studyPermissionService;

    @MockBean
    StudyService studyService;

    @MockBean
    OAuth2AuthenticationService authService;

    @InjectMocks
    ControllerPermissionSecurity controllerPermissionSecurity;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    private final AuthenticatedUser authUser = new AuthenticatedUser(
            UUID.randomUUID().toString(),
            "More User",
            "more@example.com",
            "The Hospital",
            EnumSet.allOf(PlatformRole.class));

    @Test
    void testRoleChecksDenied() throws Exception{
        when(authService.getCurrentUser()).thenReturn(authUser);
        when(studyPermissionService.hasAnyRole(anyLong(), anyString(), anySet())).thenReturn(false);

        CollaboratorDTO collaboratorRequest = new CollaboratorDTO().user(new UserInfoDTO().uid("d").name("n").email("email@email.com").institution("institution")).roles(Set.of());

        mvc.perform(put("/api/v1/studies/L2/Collaborators/8")
                        .content(mapper.writeValueAsString(collaboratorRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void testRoleChecksAccepted() throws Exception{
        when(authService.getCurrentUser()).thenReturn(authUser);
        when(studyPermissionService.hasAnyRole(anyLong(), anyString(), anySet())).thenReturn(true);
        when(studyService.setRolesForStudy(anyLong(),anyString(),anySet(),any(User.class)))
                .thenReturn(Optional.of(new StudyUserRoles(new MoreUser("id","name","institution","test@mail.com"),
                        Set.of(new StudyUserRoles.StudyRoleDetails(StudyRole.STUDY_ADMIN, new MoreUser("id","name","institution","test@mail.com"), Instant.now())))));

        CollaboratorDTO collaboratorRequest = new CollaboratorDTO().user(new UserInfoDTO().uid("d").name("n").email("email@email.com").institution("institution")).roles(Set.of());

        mvc.perform(put("/api/v1/studies/L2/Collaborators/8")
                        .content(mapper.writeValueAsString(collaboratorRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

    }
}
