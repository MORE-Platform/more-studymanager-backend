package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.model.AuthenticatedUser;
import io.redlink.more.studymanager.model.EndpointToken;
import io.redlink.more.studymanager.model.PlatformRole;
import io.redlink.more.studymanager.service.IntegrationService;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({IntegrationsApiV1Controller.class})
@AutoConfigureMockMvc(addFilters = false)
public class IntegrationsControllerTest {

    @MockBean
    IntegrationService integrationService;

    @MockBean
    OAuth2AuthenticationService authenticationService;

    @Autowired
    MockMvc mvc;

    @BeforeEach
    void setUp() {
        when(authenticationService.getCurrentUser()).thenReturn(
                new AuthenticatedUser(
                        UUID.randomUUID().toString(),
                        "Test User", "test@example.com", "Test Inc.",
                        EnumSet.allOf(PlatformRole.class)
                )
        );
    }

    @Test
    @DisplayName("Add token should create and return token with id, label, timestamp and secret set, only if label is valid")
    void testAddToken() throws Exception{
        EndpointToken token = new EndpointToken()
                .setTokenId(1)
                .setTokenLabel("testLabel")
                .setToken("test")
                .setCreated(Instant.now());
        when(integrationService.addToken(anyLong(), anyInt(), anyString()))
                .thenAnswer(invocationOnMock -> new EndpointToken()
                        .setTokenId(token.getTokenId())
                        .setTokenLabel(invocationOnMock.getArgument(2))
                        .setToken(token.getToken())
                        .setCreated(token.getCreated()));
        mvc.perform(post("/api/v1/studies/1/observations/1/tokens")
                .content(token.getTokenLabel())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tokenId").value(token.getTokenId()))
                .andExpect(jsonPath("$.tokenLabel").value(token.getTokenLabel()))
                .andExpect(jsonPath("$.token").value(token.getToken()))
                .andExpect(jsonPath("$.created").exists());

        mvc.perform(post("/api/v1/studies/1/observations/1/tokens")
                        .content("")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/studies/1/observations/1/tokens")
                        .content(" ")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get token should return the token with the given token_id")
    void testGetToken() throws Exception {
        EndpointToken token = new EndpointToken()
                .setTokenId(1)
                .setTokenLabel("testLabel")
                .setToken("test")
                .setCreated(Instant.now());

        when(integrationService.getToken(anyLong(), anyInt(), anyInt()))
                .thenAnswer(invocationOnMock -> new EndpointToken()
                        .setTokenId(invocationOnMock.getArgument(2))
                        .setTokenLabel(token.getTokenLabel())
                        .setToken(token.getToken())
                        .setCreated(token.getCreated()));

        mvc.perform(get("/api/v1/studies/1/observations/1/tokens/1")
                        .content(token.getTokenLabel())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenId").value(token.getTokenId()))
                .andExpect(jsonPath("$.tokenLabel").value(token.getTokenLabel()))
                .andExpect(jsonPath("$.token").value(token.getToken()))
                .andExpect(jsonPath("$.created").exists());
    }

    @Test
    @DisplayName("Get tokens should return all tokens for given observation")
    void testGetTokens() throws Exception {
        EndpointToken token1 = new EndpointToken()
                .setTokenId(1)
                .setTokenLabel("testLabel1")
                .setToken("test1")
                .setCreated(Instant.now());
        EndpointToken token2 = new EndpointToken()
                .setTokenId(2)
                .setTokenLabel("testLabel2")
                .setToken("test2")
                .setCreated(Instant.now());

        when(integrationService.getTokens(anyLong(), anyInt()))
                .thenAnswer(invocationOnMock -> List.of(
                        new EndpointToken()
                                .setTokenId(token1.getTokenId())
                                .setTokenLabel(token1.getTokenLabel())
                                .setToken(token1.getToken())
                                .setCreated(token1.getCreated()),
                        new EndpointToken()
                                .setTokenId(token2.getTokenId())
                                .setTokenLabel(token2.getTokenLabel())
                                .setToken(token2.getToken())
                                .setCreated(token2.getCreated())
                ));

        mvc.perform(get("/api/v1/studies/1/observations/1/tokens"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tokenId").value(token1.getTokenId()))
                .andExpect(jsonPath("$[0].tokenLabel").value(token1.getTokenLabel()))
                .andExpect(jsonPath("$[0].token").value(token1.getToken()))
                .andExpect(jsonPath("$[0].created").exists())
                .andExpect(jsonPath("$[1].tokenId").value(token2.getTokenId()))
                .andExpect(jsonPath("$[1].tokenLabel").value(token2.getTokenLabel()))
                .andExpect(jsonPath("$[1].token").value(token2.getToken()))
                .andExpect(jsonPath("$[1].created").exists());
    }
}
