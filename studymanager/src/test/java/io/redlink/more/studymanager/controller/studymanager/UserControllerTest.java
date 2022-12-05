package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.model.AuthenticatedUser;
import io.redlink.more.studymanager.model.MoreUser;
import io.redlink.more.studymanager.model.PlatformRole;
import io.redlink.more.studymanager.model.SearchResult;
import io.redlink.more.studymanager.model.transformer.RoleTransformer;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import io.redlink.more.studymanager.service.UserService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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

    @MockBean
    UserService userService;

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
                                RoleTransformer.toPlatformRoleDTO(PlatformRole.MORE_OPERATOR).getValue())
                ))
        ;
    }

    @Test
    void testUserSearch() throws Exception {
        MoreUser u1 = createUser("Derdust", "Lumwinn", "Old Republic");
        MoreUser u2 = createUser("Noellam", "Lambed", "Old Republic");
        when(userService.findUsers(any(), anyInt(), anyInt()))
                .thenReturn(new SearchResult<>(12, 5, List.of(u1, u2)));

        mvc.perform(get("/api/v1/users/search")
                        .queryParam("q", "L")
                        .queryParam("offset", "5")
                        .queryParam("limit", "2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query.q").value("L"))
                .andExpect(jsonPath("$.query.offset").value("5"))
                .andExpect(jsonPath("$.query.limit").value("2"))
                .andExpect(jsonPath("$.result.numFound").value("12"))
                .andExpect(jsonPath("$.result.start").value("5"))
        ;
    }

    MoreUser createUser(String firstName, String lastName, String institution) {
        return new MoreUser(
                UUID.randomUUID().toString(),
                firstName + " " + lastName,
                String.format("%s.%s@%s.com",
                        firstName.toLowerCase(Locale.ROOT),
                        lastName.toLowerCase(Locale.ROOT),
                        URLEncoder.encode(institution.toLowerCase(Locale.ROOT), StandardCharsets.UTF_8)),
                institution,
                Instant.now(),
                Instant.now()
        );

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