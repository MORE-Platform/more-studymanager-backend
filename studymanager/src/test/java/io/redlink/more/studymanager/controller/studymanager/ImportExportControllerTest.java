package io.redlink.more.studymanager.controller.studymanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.redlink.more.studymanager.service.ImportExportService;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;


@WebMvcTest({ImportExportApiV1Controller.class})
@AutoConfigureMockMvc(addFilters = false)
class ImportExportControllerTest {

    @MockBean
    ImportExportService importExportService;

    @MockBean
    OAuth2AuthenticationService oAuth2AuthenticationService;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("Participants should be exported in csv format as a Resource")
    void testExportParticipants() throws Exception {

        String csv = "STUDYID;TITLE;PARTICIPANTID;ALIAS;REGISTRATIONTOKEN\n1;Study;1;SomeAlias;SomeToken";

        when(importExportService.exportParticipants(any(Long.class), any()))
                .thenAnswer(invocationOnMock -> new ByteArrayResource(csv.getBytes(StandardCharsets.UTF_8)));

        MvcResult result = mvc.perform(get("/api/v1/studies/1/export/participants")
                        .contentType("text/csv"))
                .andDo(print())
                .andReturn();
        assertThat(result.getResponse().getContentAsString()).isEqualTo(csv);
    }
}
