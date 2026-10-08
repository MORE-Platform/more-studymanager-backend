/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.component.observation.lime;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.redlink.more.studymanager.component.observation.lime.model.AnswerOptionData;
import io.redlink.more.studymanager.component.observation.lime.model.ParticipantCreationData;
import io.redlink.more.studymanager.component.observation.lime.model.QuestionData;
import io.redlink.more.studymanager.component.observation.lime.model.QuestionGroupData;
import io.redlink.more.studymanager.component.observation.lime.model.SubQuestionData;
import io.redlink.more.studymanager.component.observation.lime.model.SurveyStructure;
import io.redlink.more.studymanager.component.observation.lime.model.UploadedFile;
import io.redlink.more.studymanager.core.factory.ComponentFactoryProperties;
import io.redlink.more.studymanager.core.survey.ResponseSelection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Flow;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class LimeSurveyRequestServiceTest {

    private final ComponentFactoryProperties properties = limeProperties();
    private final HttpClient client = mock(HttpClient.class);
    private final LimeSurveyRequestService service = new LimeSurveyRequestService(client, properties);

    /** The credentials and remote url every remote-control call needs; the client itself is mocked. */
    private static ComponentFactoryProperties limeProperties() {
        ComponentFactoryProperties properties = new ComponentFactoryProperties();
        properties.put("username", "more-admin");
        properties.put("password", "more-admin");
        properties.put("remoteUrl", "https://lime.example.org/admin/remotecontrol");
        properties.put("baseUrl", "https://lime.example.org");
        return properties;
    }

    @Test
    void parseRequestTest() throws JsonProcessingException {
        Assertions.assertEquals(service.parseRequest("get_session_key",
                List.of("username",
                        "password")),
                """
                        {"method":"get_session_key","params":["username","password"],"id":1}""");

        Assertions.assertEquals("""
                        {"method":"add_participants","params":[1,2,[{"firstname":"3","lastname":"4"},{"firstname":"5","lastname":"6"}]],"id":1}""",
                service.parseRequest("add_participants",
                        List.of(
                                1,
                                2,
                                List.of(
                                        new ParticipantCreationData("3", "4", null),
                                        new ParticipantCreationData("5", "6", null)))));

        Assertions.assertEquals(service.parseRequest("activate_tokens", List.of("1", "2")),
                """
                        {"method":"activate_tokens","params":["1","2"],"id":1}""");
    }

    @Test
    void fixNullDateTest() {
        Map<String, Object> answer = new HashMap<>();
        answer.put("Date submitted", "1980-01-01 00:00:00");
        answer.put("other", "value");

        service.fixNullDate(answer);

        Assertions.assertNotEquals("1980-01-01 00:00:00", answer.get("Date submitted"));
        Assertions.assertEquals("value", answer.get("other"));
        // Check if it's a valid date format
        Assertions.assertTrue(((String) answer.get("Date submitted")).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    // --- survey structure -------------------------------------------------------------------------------

    @Test
    void getSurveyStructureReadsGroupsQuestionsAndOptions() throws IOException, InterruptedException {
        stubLimeSurvey(Map.of(
                "list_groups", """
                        {"id":1,"error":null,"result":[
                          {"gid":2,"group_name":"Second","description":"later","group_order":2},
                          {"gid":1,"group_name":"First","description":"earlier","group_order":1}]}""",
                "list_questions", """
                        {"id":1,"error":null,"result":[
                          {"qid":11,"gid":1,"title":"G01Q02","question":"Why?","type":"T","question_order":2,"parent_qid":"0","mandatory":"N"},
                          {"qid":10,"gid":1,"title":"Q00","question":"Pick all","type":"M","question_order":1,"parent_qid":"0","mandatory":"Y"},
                          {"qid":99,"gid":1,"title":"SQ001","question":"a subquestion","type":"T","question_order":1,"parent_qid":"10","mandatory":"N"},
                          {"qid":12,"gid":2,"title":"Q10","question":"Rate","type":"L","question_order":1,"parent_qid":"0","mandatory":"N"}]}""",
                "get_question_properties:10", """
                        {"id":1,"error":null,"result":{"title":"Q00","question":"Pick all","type":"M","question_order":1,"mandatory":"Y",
                          "answeroptions":"No available answer options",
                          "subquestions":{"7":{"title":"SQ002","question":"Second","question_order":2},
                                          "6":{"title":"SQ001","question":"First","question_order":1}}}}""",
                "get_question_properties:11", """
                        {"id":1,"error":null,"result":{"title":"G01Q02","question":"Why?","type":"T","question_order":2,"mandatory":"N",
                          "answeroptions":"No available answer options","subquestions":"No available answers"}}""",
                "get_question_properties:12", """
                        {"id":1,"error":null,"result":{"title":"Q10","question":"Rate","type":"L","question_order":1,"mandatory":"N",
                          "answeroptions":{"N":{"answer":"No","order":2},"Y":{"answer":"Yes","order":1}},
                          "subquestions":"No available answers"}}"""
        ));

        SurveyStructure structure = service.getSurveyStructure("976294");

        Assertions.assertEquals("976294", structure.surveyId());
        Assertions.assertEquals("en", structure.language());
        Assertions.assertEquals(List.of("First", "Second"),
                structure.groups().stream().map(QuestionGroupData::title).toList());

        QuestionGroupData first = structure.groups().get(0);
        // question_order decides, and the subquestion (parent_qid 10) is not listed as a question of its own
        Assertions.assertEquals(List.of("Q00", "G01Q02"),
                first.questions().stream().map(QuestionData::code).toList());

        QuestionData multipleChoice = first.questions().get(0);
        Assertions.assertTrue(multipleChoice.mandatory());
        Assertions.assertEquals(List.of("SQ001", "SQ002"),
                multipleChoice.subQuestions().stream().map(SubQuestionData::code).toList());
        Assertions.assertEquals(List.of("First", "Second"),
                multipleChoice.subQuestions().stream().map(SubQuestionData::text).toList());
        Assertions.assertTrue(multipleChoice.answerOptions().isEmpty(),
                "LimeSurvey reports missing options as a string, which must yield no options");

        QuestionData freeText = first.questions().get(1);
        Assertions.assertFalse(freeText.mandatory());
        Assertions.assertTrue(freeText.answerOptions().isEmpty());

        QuestionData list = structure.groups().get(1).questions().get(0);
        Assertions.assertEquals(List.of("Y", "N"), list.answerOptions().stream().map(AnswerOptionData::code).toList());
        Assertions.assertEquals(List.of("Yes", "No"), list.answerOptions().stream().map(AnswerOptionData::label).toList());
    }

    @Test
    void getSurveyStructureResolvesRelativeImageUrls() throws IOException, InterruptedException {
        stubLimeSurvey(Map.of(
                "list_groups", """
                        {"id":1,"error":null,"result":[{"gid":1,"group_name":"First","description":"<img src=\\"/upload/g.png\\">","group_order":1}]}""",
                "list_questions", """
                        {"id":1,"error":null,"result":[
                          {"qid":10,"gid":1,"title":"Q00","question":"q","type":"L","question_order":1,"parent_qid":"0","mandatory":"N"}]}""",
                "get_question_properties:10", """
                        {"id":1,"error":null,"result":{"title":"Q00","type":"L","question_order":1,"mandatory":"N",
                          "question":"Which one? <img src=\\"/upload/surveys/1/images/x.png\\"> and <img src=\\"https://cdn.example.org/y.png\\">",
                          "answeroptions":{"Y":{"answer":"<img src='/upload/yes.png'>Yes","order":1}},
                          "subquestions":{"6":{"title":"SQ001","question":"<img src=\\"/upload/sq.png\\">","question_order":1}}}}"""
        ));

        QuestionData question = service.getSurveyStructure("976294").groups().get(0).questions().get(0);

        Assertions.assertEquals(
                "Which one? <img src=\"https://lime.example.org/upload/surveys/1/images/x.png\"> "
                        + "and <img src=\"https://cdn.example.org/y.png\">",
                question.text(),
                "host-relative images become absolute, absolute ones stay untouched");
        Assertions.assertEquals("<img src='https://lime.example.org/upload/yes.png'>Yes",
                question.answerOptions().get(0).label());
        Assertions.assertEquals("<img src=\"https://lime.example.org/upload/sq.png\">",
                question.subQuestions().get(0).text());
        Assertions.assertEquals("<img src=\"https://lime.example.org/upload/g.png\">",
                service.getSurveyStructure("976294").groups().get(0).description());
    }

    // --- uploaded files ---------------------------------------------------------------------------------

    @Test
    void getUploadedFilesDecodesTheBase64Content() throws IOException, InterruptedException {
        String content = Base64.getEncoder().encodeToString("an image".getBytes(StandardCharsets.UTF_8));
        stubLimeSurvey(Map.of("get_uploaded_files", """
                {"id":1,"error":null,"result":{"fu_abc123":{
                  "meta":{"name":"photo.jpg","ext":"jpg","size":"124.5","question":{"title":"Q07","qid":7}},
                  "content":"%s"}}}""".formatted(content)));

        List<UploadedFile> files = service.getUploadedFiles(976294, "token", 3);

        Assertions.assertEquals(1, files.size());
        UploadedFile file = files.get(0);
        Assertions.assertEquals("fu_abc123", file.filename());
        Assertions.assertEquals("photo.jpg", file.name());
        Assertions.assertEquals("jpg", file.extension());
        Assertions.assertEquals("Q07", file.questionCode());
        Assertions.assertEquals("an image", new String(file.content(), StandardCharsets.UTF_8));
    }

    @Test
    void getUploadedFilesTreatsNoFilesFoundAsEmpty() throws IOException, InterruptedException {
        stubLimeSurvey(Map.of("get_uploaded_files", """
                {"id":1,"error":null,"result":{"status":"No files found"}}"""));

        Assertions.assertTrue(service.getUploadedFiles(976294, "token", 3).isEmpty());
        Assertions.assertTrue(service.getUploadedFiles(976294, " ", 3).isEmpty(), "no token, no call");
        Assertions.assertTrue(service.getUploadedFiles(0, "token", 3).isEmpty());
    }

    @Test
    void getSurveyStructureTreatsNoGroupsFoundAsEmpty() throws IOException, InterruptedException {
        stubLimeSurvey(Map.of(
                "list_groups", """
                        {"id":1,"error":null,"result":{"status":"No groups found"}}""",
                "list_questions", """
                        {"id":1,"error":null,"result":{"status":"No questions found"}}"""
        ));

        SurveyStructure structure = service.getSurveyStructure("976294");

        Assertions.assertTrue(structure.groups().isEmpty());
        Assertions.assertEquals("en", structure.language());
    }

    @Test
    void getSurveyStructureRejectsBlankSurveyId() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> service.getSurveyStructure(" "));
    }

    // --- responses --------------------------------------------------------------------------------------

    @Test
    void getAnswersSelectsAllLatestOrOneResponse() throws IOException, InterruptedException {
        stubLimeSurvey(Map.of("export_responses_by_token", exportResponse("""
                [{"id":"2","seed":"222","submitdate":"2026-10-02 09:00:00","G01Q02":"second"},
                 {"id":"1","seed":"111","submitdate":"2026-10-01 09:00:00","G01Q02":"first"}]""")));

        List<Map<String, Object>> all = service.getAnswers("token", 976294, ResponseSelection.all());
        Assertions.assertEquals(List.of("first", "second"), all.stream().map(a -> a.get("G01Q02")).toList(),
                "all responses are returned oldest first");

        List<Map<String, Object>> latest = service.getAnswers("token", 976294, ResponseSelection.latest());
        Assertions.assertEquals(1, latest.size());
        Assertions.assertEquals("second", latest.get(0).get("G01Q02"));

        List<Map<String, Object>> single = service.getAnswers("token", 976294, ResponseSelection.bySavedId(1));
        Assertions.assertEquals(1, single.size());
        Assertions.assertEquals("first", single.get(0).get("G01Q02"));

        Assertions.assertTrue(service.getAnswers("token", 976294, ResponseSelection.bySavedId(42)).isEmpty());
    }

    @Test
    void getAnswersStripsTheToken() throws IOException, InterruptedException {
        stubLimeSurvey(Map.of("export_responses_by_token", exportResponse("""
                [{"id":"1","token":"secret","G01Q02":"answer","mirrored":"secret","empty":null}]""")));

        Map<String, Object> answer = service.getAnswers("secret", 976294, ResponseSelection.all()).get(0);

        Assertions.assertEquals("answer", answer.get("G01Q02"));
        Assertions.assertFalse(answer.containsKey("token"));
        Assertions.assertFalse(answer.containsValue("secret"));
        Assertions.assertFalse(answer.containsKey("empty"));
    }

    @Test
    void getAnswerStillReturnsASoleResponseWithANonMatchingId() throws IOException, InterruptedException {
        stubLimeSurvey(Map.of("export_responses_by_token", exportResponse("""
                [{"id":"7","G01Q02":"only one"}]""")));

        // pinned behaviour: the sidecar write path relies on a single response being accepted as-is
        Optional<Map<String, Object>> answer = service.getAnswer("token", 976294, 3);

        Assertions.assertTrue(answer.isPresent());
        Assertions.assertEquals("only one", answer.get().get("G01Q02"));
    }

    @Test
    void getAnswerPicksTheMatchingResponseWhenThereAreSeveral() throws IOException, InterruptedException {
        stubLimeSurvey(Map.of("export_responses_by_token", exportResponse("""
                [{"id":"7","G01Q02":"seven"},{"id":"3","G01Q02":"three"}]""")));

        Assertions.assertEquals("three", service.getAnswer("token", 976294, 3).orElseThrow().get("G01Q02"));
        Assertions.assertTrue(service.getAnswer("token", 976294, 42).isEmpty());
    }

    @Test
    void getAnswersRejectsInvalidInput() throws IOException, InterruptedException {
        stubLimeSurvey(Map.of());

        Assertions.assertTrue(service.getAnswers(" ", 976294, ResponseSelection.all()).isEmpty());
        Assertions.assertTrue(service.getAnswers("token", 0, ResponseSelection.all()).isEmpty());
        Assertions.assertTrue(service.getAnswer("token", 976294, 0).isEmpty());
    }

    // --- stubbing helpers -------------------------------------------------------------------------------

    /** Base64 payload LimeSurvey wraps an {@code export_responses_by_token} result in. */
    private static String exportResponse(String responsesJson) {
        String payload = "{\"responses\":" + responsesJson + "}";
        return "{\"id\":1,\"error\":null,\"result\":\""
                + Base64.getEncoder().encodeToString(payload.getBytes(StandardCharsets.UTF_8)) + "\"}";
    }

    /**
     * Routes the mocked client by the RPC method of each request, so tests declare responses by method
     * instead of by call order. {@code get_question_properties} is keyed as {@code method:<qid>}.
     */
    private void stubLimeSurvey(Map<String, String> responsesByMethod) throws IOException, InterruptedException {
        Map<String, String> responses = new HashMap<>(responsesByMethod);
        responses.putIfAbsent("get_session_key", """
                {"id":1,"error":null,"result":"session-key"}""");
        responses.putIfAbsent("get_language_properties", """
                {"id":1,"error":null,"result":{"surveyls_language":"en"}}""");
        responses.putIfAbsent("release_session_key", """
                {"id":1,"error":null,"result":"OK"}""");

        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenAnswer(invocation -> {
            String body = requestBody(invocation.getArgument(0));
            String key = routingKey(body);
            String response = responses.get(key);
            Assertions.assertNotNull(response, "no stubbed LimeSurvey response for '" + key + "', request was " + body);

            HttpResponse<String> httpResponse = mock(HttpResponse.class);
            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn(response);
            return httpResponse;
        });
    }

    private static final Pattern METHOD = Pattern.compile("\"method\":\"([^\"]+)\"");
    private static final Pattern QUESTION_ID = Pattern.compile("\"params\":\\[[^,]+,(\\d+)");

    private static String routingKey(String body) {
        Matcher method = METHOD.matcher(body);
        if (!method.find()) {
            return body;
        }
        if (!"get_question_properties".equals(method.group(1))) {
            return method.group(1);
        }
        Matcher questionId = QUESTION_ID.matcher(body);
        return questionId.find() ? method.group(1) + ":" + questionId.group(1) : method.group(1);
    }

    /** Reads back the JSON a request was built with; {@code BodyPublishers.ofString} delivers synchronously. */
    private static String requestBody(HttpRequest request) {
        StringBuilder body = new StringBuilder();
        request.bodyPublisher().orElseThrow().subscribe(new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(ByteBuffer item) {
                body.append(StandardCharsets.UTF_8.decode(item));
            }

            @Override
            public void onError(Throwable throwable) {
                throw new IllegalStateException(throwable);
            }

            @Override
            public void onComplete() {
                // nothing to do
            }
        });
        return body.toString();
    }
}
