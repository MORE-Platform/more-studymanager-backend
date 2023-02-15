package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ComponentApiV1Controller.class})
@AutoConfigureMockMvc(addFilters = false)
public class ComponentControllerTest {

    @MockBean
    private OAuth2AuthenticationService authenticationService;
    @Autowired
    private MockMvc mvc;

    @Test
    void testComponentSpecificEndpointDoesNotExist() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/api/v1/components/observation/my-test-observation/api/my-test-slug")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }
}
