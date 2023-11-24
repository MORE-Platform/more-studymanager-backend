/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.model.AttributeMapClaimAccessor;
import io.redlink.more.studymanager.model.MoreUser;
import io.redlink.more.studymanager.model.PlatformRole;
import io.redlink.more.studymanager.model.SearchResult;
import io.redlink.more.studymanager.model.transformer.RoleTransformer;
import io.redlink.more.studymanager.properties.MoreAuthProperties;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import io.redlink.more.studymanager.service.UserService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({UserApiV1Controller.class})
@EnableConfigurationProperties(MoreAuthProperties.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "more.auth.claims.roles=my_roles")
class UserControllerTest {

    @Autowired
    private MoreAuthProperties moreAuthProperties;

    @SpyBean
    @Autowired
    OAuth2AuthenticationService authService;

    @MockBean
    UserService userService;

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("Retrieve the current User")
    void testMe() throws Exception {
        final String issuer = "https://example.com/realms/foo",
                account = "https://example.com/realms/foo/account",
                admin = "https://example.com/admin/foo/console";
        var firstClaim = new AttributeMapClaimAccessor(
                createClaims(issuer, "More", "User", "Redlink",
                        PlatformRole.MORE_OPERATOR)
        );
        var secondClaim = new AttributeMapClaimAccessor(
                createClaims(issuer, "Genius", "Researcher", "DHP",
                        PlatformRole.MORE_ADMIN, PlatformRole.MORE_VIEWER)
        );
        when(authService.getClaimAccessor()).thenReturn(firstClaim, secondClaim);

        mvc.perform(get("/api/v1/users/me"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(firstClaim.getClaimAsString(StandardClaimNames.NAME)))
                .andExpect(jsonPath("$.email").value(firstClaim.getClaimAsString(StandardClaimNames.EMAIL)))
                .andExpect(jsonPath("$.institution").value(firstClaim.getClaimAsString(moreAuthProperties.claims().institution())))
                .andExpect(jsonPath("$.roles").value(
                        Matchers.contains(
                                RoleTransformer.toPlatformRoleDTO(PlatformRole.MORE_OPERATOR).getValue())
                ))
                .andExpect(jsonPath("$.links.profile").value(account))
                .andExpect(jsonPath("$.links.userManagement").value(admin))
        ;

        mvc.perform(get("/api/v1/users/me"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(secondClaim.getClaimAsString(StandardClaimNames.NAME)))
                .andExpect(jsonPath("$.email").value(secondClaim.getClaimAsString(StandardClaimNames.EMAIL)))
                .andExpect(jsonPath("$.institution").value(secondClaim.getClaimAsString(moreAuthProperties.claims().institution())))
                .andExpect(jsonPath("$.roles").value(
                        Matchers.containsInAnyOrder(
                                RoleTransformer.toPlatformRoleDTO(PlatformRole.MORE_ADMIN).getValue(),
                                RoleTransformer.toPlatformRoleDTO(PlatformRole.MORE_VIEWER).getValue()
                        )
                ))
                .andExpect(jsonPath("$.links.profile").value(account))
                .andExpect(jsonPath("$.links.userManagement").value(admin))
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

    Map<String, Object> createClaims(String issuer, String firstName, String lastName, String institution, PlatformRole... roles) {
        return Map.of(
                IdTokenClaimNames.ISS, issuer,
                StandardClaimNames.SUB, UUID.randomUUID().toString(),
                StandardClaimNames.GIVEN_NAME, firstName,
                StandardClaimNames.FAMILY_NAME, lastName,
                StandardClaimNames.NAME, firstName + " " + lastName,
                StandardClaimNames.EMAIL,
                String.format("%s.%s@%s.com",
                        firstName.toLowerCase(Locale.ROOT),
                        lastName.toLowerCase(Locale.ROOT),
                        URLEncoder.encode(institution.toLowerCase(Locale.ROOT), StandardCharsets.UTF_8)),
                StandardClaimNames.EMAIL_VERIFIED, true,
                moreAuthProperties.claims().institution(), institution,
                moreAuthProperties.claims().roles(),
                Set.of(roles).stream()
                        .flatMap(r -> moreAuthProperties.globalRoles().getOrDefault(r, Set.of()).stream())
                        .collect(Collectors.toUnmodifiableSet())
        );
    }

}