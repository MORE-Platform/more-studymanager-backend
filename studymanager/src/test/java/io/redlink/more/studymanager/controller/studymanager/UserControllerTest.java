package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.model.AuthenticatedUser;
import io.redlink.more.studymanager.model.PlatformRole;
import io.redlink.more.studymanager.model.transformer.UserInfoTransformer;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({UserApiV1Controller.class})
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @MockBean
    OAuth2AuthenticationService authService;

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("Retrieve the current User")
    void testMe() throws Exception {
        var authUser = createUser("More", "User", "Redlink", PlatformRole.MORE_OPERATOR);
        when(authService.getCurrentUser()).thenReturn(authUser);

        mvc.perform(get("/api/v1/users/me"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(authUser.fullName()))
                .andExpect(jsonPath("$.email").value(authUser.email()))
                .andExpect(jsonPath("$.institution").value(authUser.institution()))
                .andExpect(jsonPath("$.roles").value(
                        Matchers.contains(
                                UserInfoTransformer.toPlatformRole(PlatformRole.MORE_OPERATOR).getValue())
                        ))
        ;
    }


    AuthenticatedUser createUser(String firstName, String lastName, String institution, PlatformRole... roles) {
        return new AuthenticatedUser(
                UUID.randomUUID().toString(),
                firstName + " " + lastName,
                String.format("%s.%s@%s.com",
                        firstName.toLowerCase(Locale.ROOT),
                        lastName.toLowerCase(Locale.ROOT),
                        URLEncoder.encode(institution.toLowerCase(Locale.ROOT), StandardCharsets.UTF_8)),
                institution,
                Set.of(roles)
        );
    }

}