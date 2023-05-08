package io.redlink.more.studymanager.component.observation.lime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.redlink.more.studymanager.component.observation.lime.LimeSurveyProperties;
import io.redlink.more.studymanager.component.observation.lime.LimeSurveyRequestService;
import io.redlink.more.studymanager.component.observation.lime.model.LimeSurveyParticipantResponse;
import io.redlink.more.studymanager.component.observation.lime.model.ParticipantData;
import io.redlink.more.studymanager.core.factory.ComponentFactoryProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;


public class LimeSurveyRequestServiceTest {

    private final ComponentFactoryProperties properties = new ComponentFactoryProperties();
    private final HttpClient client = mock(HttpClient.class);
    private final LimeSurveyRequestService service = new LimeSurveyRequestService(client, properties);

    @Test
    void parseRequestTest() throws JsonProcessingException {
        Assertions.assertEquals(service.parseRequest("get_session_key",
                List.of("username",
                        "password")),
                """
                        {"method":"get_session_key","params":["username","password"],"id":1}""");

        Assertions.assertEquals(service.parseRequest("add_participants",
                        List.of(
                                1,
                                2,
                                List.of(
                                        new ParticipantData("3", "4", null),
                                        new ParticipantData("5", "6", null)))),
                """
                        {"method":"add_participants","params":[1,2,[{"firstname":"3","lastname":"4"},{"firstname":"5","lastname":"6"}]],"id":1}""");

        Assertions.assertEquals(service.parseRequest("activate_tokens", List.of("1", "2")),
                """
                        {"method":"activate_tokens","params":["1","2"],"id":1}""");
    }

    @Test
    void responseTest() throws JsonProcessingException {
        String response = """
                {"id":1,"result": [
                    		{
                    			"sent": "N",
                    			"remindersent": "N",
                    			"remindercount": 0,
                    			"completed": "N",
                    			"usesleft": 1,
                    			"emailstatus": "OK",
                    			"lastname": "test1",
                    			"firstname": "test1",
                    			"token": "1",
                    			"language": "",
                    			"email": "",
                    			"tid": "1",
                    			"participant_id": null,
                    			"blacklisted": null,
                    			"validfrom": null,
                    			"validuntil": null,
                    			"mpid": null
                    		},
                    		{
                    			"sent": "N",
                    			"remindersent": "N",
                    			"remindercount": 0,
                    			"completed": "N",
                    			"usesleft": 1,
                    			"emailstatus": "OK",
                    			"lastname": "test2",
                    			"firstname": "test2",
                    			"token": "2",
                    			"language": "",
                    			"email": "",
                    			"tid": "1",
                    			"participant_id": null,
                    			"blacklisted": null,
                    			"validfrom": null,
                    			"validuntil": null,
                    			"mpid": null
                    		}
                    	],
                    	"error": null
                }""";
        List<ParticipantData> expectedList = new ArrayList<>();
        expectedList.add(new ParticipantData("test1", "test1", "1"));
        expectedList.add(new ParticipantData("test2", "test2", "2"));
        Assertions.assertEquals(new ObjectMapper().readValue(response, LimeSurveyParticipantResponse.class).result(),
                expectedList);
    }
}
