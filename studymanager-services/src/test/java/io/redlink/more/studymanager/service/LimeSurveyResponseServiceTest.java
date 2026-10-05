/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache License, Version 2.0.
 */
package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.component.observation.lime.LimeSurveyObservationFactory;
import io.redlink.more.studymanager.component.observation.lime.LimeSurveyRequestService;
import io.redlink.more.studymanager.component.observation.lime.model.AnswerOptionData;
import io.redlink.more.studymanager.component.observation.lime.model.QuestionData;
import io.redlink.more.studymanager.component.observation.lime.model.QuestionGroupData;
import io.redlink.more.studymanager.component.observation.lime.model.SubQuestionData;
import io.redlink.more.studymanager.component.observation.lime.model.SurveyStructure;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.survey.ResponseSelection;
import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.model.data.StoredSurveyResponse;
import io.redlink.more.studymanager.model.survey.Answer;
import io.redlink.more.studymanager.model.survey.AnsweredGroup;
import io.redlink.more.studymanager.model.survey.AnsweredQuestion;
import io.redlink.more.studymanager.model.survey.ParticipantSurveyResponses;
import io.redlink.more.studymanager.model.survey.SurveyResponse;
import io.redlink.more.studymanager.sdk.MoreSDK;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LimeSurveyResponseServiceTest {

    private static final long STUDY_ID = 97L;
    private static final int OBSERVATION_ID = 3;
    private static final int PARTICIPANT_ID = 5;
    private static final String SURVEY_ID = "976294";

    private final ObservationService observationService = mock(ObservationService.class);
    private final ElasticDataService elasticDataService = mock(ElasticDataService.class);
    private final MoreSDK sdk = mock(MoreSDK.class);
    private final LimeSurveyRequestService requestService = mock(LimeSurveyRequestService.class);

    private LimeSurveyResponseService service;

    @BeforeEach
    void init() {
        LimeSurveyObservationFactory factory = mock(LimeSurveyObservationFactory.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<LimeSurveyObservationFactory> provider = mock(ObjectProvider.class);
        //lenient: the tests that reject an observation never get as far as the request service
        lenient().when(factory.getRequestService()).thenReturn(requestService);
        lenient().when(provider.getObject()).thenReturn(factory);

        service = new LimeSurveyResponseService(observationService, elasticDataService, sdk, provider);
    }

    @Test
    @DisplayName("assigns values to questions by code, including subquestion and other cells")
    void matchesAnswersByQuestionCode() throws IOException {
        givenObservation();
        givenStructure(structure());
        givenStoredResponse(1, "1246038133", values(
                "G01Q02", "Meh I gues",
                "Q00[SQ001]", "Y",
                "Q00[SQ002]", "Y",
                "Q00[other]", "something else",
                "Q10", "Y",
                // LimeSurvey metadata, belongs to no question
                "id", 1,
                "seed", "1246038133",
                "lastpage", 1,
                "startlanguage", "en"
        ));

        ParticipantSurveyResponses result = service.getParticipantResponses(
                STUDY_ID, OBSERVATION_ID, PARTICIPANT_ID, ResponseSelection.all());

        assertThat(result.surveyId()).isEqualTo(SURVEY_ID);
        assertThat(result.language()).isEqualTo("en");
        assertThat(result.responses()).hasSize(1);

        SurveyResponse response = result.responses().get(0);
        assertThat(response.responseId()).isEqualTo(1);
        assertThat(response.seed()).isEqualTo("1246038133");
        assertThat(response.source()).isEqualTo(SurveyResponse.Source.STORED);

        AnsweredQuestion multipleChoice = question(response, "Q00");
        assertThat(multipleChoice.answers())
                .extracting(Answer::subQuestionCode, Answer::subQuestionText, Answer::value, Answer::label)
                .containsExactly(
                        tuple3("SQ001", "First", "Y", "Yes"),
                        tuple3("SQ002", "Second", "Y", "Yes"),
                        tuple3("other", null, "something else", null));

        AnsweredQuestion freeText = question(response, "G01Q02");
        assertThat(freeText.answers()).hasSize(1);
        assertThat(freeText.answers().get(0).subQuestionCode()).isNull();
        assertThat(freeText.answers().get(0).value()).isEqualTo("Meh I gues");
        assertThat(freeText.answers().get(0).label())
                .as("free text has no answer option to resolve")
                .isNull();
    }

    @Test
    @DisplayName("a shorter question code does not claim the values of a longer one")
    void doesNotConfuseQ1WithQ10() throws IOException {
        givenObservation();
        givenStructure(new SurveyStructure(SURVEY_ID, "en", List.of(
                new QuestionGroupData(1, "Group", null, 1, List.of(
                        question(1, "Q1", "short", List.of(), List.of()),
                        question(2, "Q10", "long", List.of(), List.of()))))));
        givenStoredResponse(1, "seed", values("Q1", "one", "Q10", "ten"));

        SurveyResponse response = service.getParticipantResponses(
                STUDY_ID, OBSERVATION_ID, PARTICIPANT_ID, ResponseSelection.all()).responses().get(0);

        assertThat(question(response, "Q1").answers()).singleElement()
                .extracting(Answer::value).isEqualTo("one");
        assertThat(question(response, "Q10").answers()).singleElement()
                .extracting(Answer::value).isEqualTo("ten");
    }

    @Test
    @DisplayName("keeps unanswered questions so they can still be rendered")
    void keepsUnansweredQuestions() throws IOException {
        givenObservation();
        givenStructure(structure());
        givenStoredResponse(1, "seed", values("G01Q02", "only this one"));

        SurveyResponse response = service.getParticipantResponses(
                STUDY_ID, OBSERVATION_ID, PARTICIPANT_ID, ResponseSelection.all()).responses().get(0);

        assertThat(response.groups()).hasSize(2);
        assertThat(question(response, "G01Q02").isAnswered()).isTrue();
        assertThat(question(response, "Q00").isAnswered()).isFalse();
        assertThat(question(response, "Q10").isAnswered()).isFalse();
    }

    @Test
    @DisplayName("returns several responses newest first")
    void ordersResponsesNewestFirst() throws IOException {
        givenObservation();
        givenStructure(structure());
        when(elasticDataService.listSurveyResponses(anyLong(), any(), anyInt(), anyInt(), any()))
                .thenReturn(List.of(
                        stored(1, "first", Instant.parse("2026-10-01T09:00:00Z")),
                        stored(2, "second", Instant.parse("2026-10-02T09:00:00Z"))));

        ParticipantSurveyResponses result = service.getParticipantResponses(
                STUDY_ID, OBSERVATION_ID, PARTICIPANT_ID, ResponseSelection.all());

        assertThat(result.responses()).extracting(SurveyResponse::responseId).containsExactly(2, 1);
    }

    @Test
    @DisplayName("falls back to LimeSurvey when nothing is stored for the participant")
    void fallsBackToLimeSurvey() throws IOException {
        givenObservation();
        givenStructure(structure());
        when(elasticDataService.listSurveyResponses(anyLong(), any(), anyInt(), anyInt(), any()))
                .thenReturn(List.of());
        when(sdk.getPropertiesForParticipant(STUDY_ID, PARTICIPANT_ID, OBSERVATION_ID))
                .thenReturn(Optional.of(new ObservationProperties(Map.of("token", "tok-1"))));
        when(requestService.getAnswers(anyString(), anyInt(), any()))
                .thenReturn(List.of(values(
                        "id", "4", "seed", "999", "submitdate", "2026-10-03 11:22:33",
                        "G01Q02", "from lime")));

        ParticipantSurveyResponses result = service.getParticipantResponses(
                STUDY_ID, OBSERVATION_ID, PARTICIPANT_ID, ResponseSelection.all());

        verify(requestService).getAnswers("tok-1", 976294, ResponseSelection.all());

        SurveyResponse response = result.responses().get(0);
        assertThat(response.source()).isEqualTo(SurveyResponse.Source.LIME);
        assertThat(response.responseId()).isEqualTo(4);
        assertThat(response.seed()).isEqualTo("999");
        assertThat(response.submitted()).isEqualTo(Instant.parse("2026-10-03T11:22:33Z"));
        assertThat(question(response, "G01Q02").answers()).singleElement()
                .extracting(Answer::value).isEqualTo("from lime");
    }

    @Test
    @DisplayName("does not query LimeSurvey responses when stored data exists")
    void prefersStoredData() throws IOException {
        givenObservation();
        givenStructure(structure());
        givenStoredResponse(1, "seed", values("G01Q02", "stored"));

        service.getParticipantResponses(STUDY_ID, OBSERVATION_ID, PARTICIPANT_ID, ResponseSelection.all());

        verify(requestService, never()).getAnswers(anyString(), anyInt(), any());
    }

    @Test
    @DisplayName("returns no responses when the participant has no LimeSurvey token")
    void returnsEmptyWithoutAToken() throws IOException {
        givenObservation();
        givenStructure(structure());
        when(elasticDataService.listSurveyResponses(anyLong(), any(), anyInt(), anyInt(), any()))
                .thenReturn(List.of());
        when(sdk.getPropertiesForParticipant(STUDY_ID, PARTICIPANT_ID, OBSERVATION_ID))
                .thenReturn(Optional.empty());

        ParticipantSurveyResponses result = service.getParticipantResponses(
                STUDY_ID, OBSERVATION_ID, PARTICIPANT_ID, ResponseSelection.all());

        assertThat(result.responses()).isEmpty();
        verify(requestService, never()).getAnswers(anyString(), anyInt(), any());
    }

    @Test
    void rejectsAnUnknownObservation() {
        when(observationService.getObservation(STUDY_ID, OBSERVATION_ID)).thenReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class).isThrownBy(() ->
                service.getParticipantResponses(STUDY_ID, OBSERVATION_ID, PARTICIPANT_ID, ResponseSelection.all()));
    }

    @Test
    void rejectsAnObservationOfAnotherType() {
        when(observationService.getObservation(STUDY_ID, OBSERVATION_ID)).thenReturn(Optional.of(
                new Observation().setStudyId(STUDY_ID).setObservationId(OBSERVATION_ID)
                        .setType("acc-mobile-observation")));

        assertThatExceptionOfType(BadRequestException.class).isThrownBy(() ->
                service.getParticipantResponses(STUDY_ID, OBSERVATION_ID, PARTICIPANT_ID, ResponseSelection.all()));
    }

    @Test
    @DisplayName("falls back to the survey id stored at activation time")
    void resolvesTheSurveyIdFromTheActivatedValue() throws IOException {
        when(observationService.getObservation(STUDY_ID, OBSERVATION_ID)).thenReturn(Optional.of(
                new Observation().setStudyId(STUDY_ID).setObservationId(OBSERVATION_ID)
                        .setType(LimeSurveyObservationFactory.COMPONENT_ID)
                        .setProperties(new ObservationProperties())));

        var scoped = mock(io.redlink.more.studymanager.core.sdk.MoreObservationSDK.class);
        when(sdk.scopedObservationSDK(STUDY_ID, null, OBSERVATION_ID)).thenReturn(scoped);
        when(scoped.getValue("limeSurveyId", String.class)).thenReturn(Optional.of(SURVEY_ID));
        givenStructure(structure());
        givenStoredResponse(1, "seed", values("G01Q02", "x"));

        ParticipantSurveyResponses result = service.getParticipantResponses(
                STUDY_ID, OBSERVATION_ID, PARTICIPANT_ID, ResponseSelection.all());

        assertThat(result.surveyId()).isEqualTo(SURVEY_ID);
    }

    @Test
    void rejectsAnObservationWithoutASurvey() {
        when(observationService.getObservation(STUDY_ID, OBSERVATION_ID)).thenReturn(Optional.of(
                new Observation().setStudyId(STUDY_ID).setObservationId(OBSERVATION_ID)
                        .setType(LimeSurveyObservationFactory.COMPONENT_ID)
                        .setProperties(new ObservationProperties())));

        var scoped = mock(io.redlink.more.studymanager.core.sdk.MoreObservationSDK.class);
        when(sdk.scopedObservationSDK(STUDY_ID, null, OBSERVATION_ID)).thenReturn(scoped);
        when(scoped.getValue("limeSurveyId", String.class)).thenReturn(Optional.empty());

        assertThatExceptionOfType(BadRequestException.class).isThrownBy(() ->
                service.getParticipantResponses(STUDY_ID, OBSERVATION_ID, PARTICIPANT_ID, ResponseSelection.all()));
    }

    // --- fixtures ---------------------------------------------------------------------------------------

    private void givenObservation() {
        when(observationService.getObservation(STUDY_ID, OBSERVATION_ID)).thenReturn(Optional.of(
                new Observation()
                        .setStudyId(STUDY_ID)
                        .setObservationId(OBSERVATION_ID)
                        .setType(LimeSurveyObservationFactory.COMPONENT_ID)
                        .setProperties(new ObservationProperties(Map.of("limeSurveyId", SURVEY_ID)))));
    }

    private void givenStructure(SurveyStructure structure) {
        when(requestService.getSurveyStructure(SURVEY_ID)).thenReturn(structure);
    }

    private void givenStoredResponse(int responseId, String seed, Map<String, Object> values) throws IOException {
        when(elasticDataService.listSurveyResponses(anyLong(), any(), anyInt(), anyInt(), any()))
                .thenReturn(List.of(new StoredSurveyResponse(
                        "limesurvey_3_976294_" + responseId, responseId, seed,
                        Instant.parse("2026-10-01T09:35:58Z"), Instant.parse("2026-10-01T09:35:58Z"), values)));
    }

    private static StoredSurveyResponse stored(int responseId, String answer, Instant effectiveTimeFrame) {
        return new StoredSurveyResponse(
                "dp-" + responseId, responseId, "seed-" + responseId, effectiveTimeFrame, effectiveTimeFrame,
                values("G01Q02", answer));
    }

    /**
     * A survey with a multiple-choice question carrying subquestions, a free-text question and a
     * single-choice question in a second group.
     */
    private static SurveyStructure structure() {
        QuestionData multipleChoice = question(10, "Q00", "Pick all",
                List.of(new SubQuestionData("SQ001", "First", 1), new SubQuestionData("SQ002", "Second", 2)),
                List.of(new AnswerOptionData("Y", "Yes", 1)));
        QuestionData freeText = question(11, "G01Q02", "Why?", List.of(), List.of());
        QuestionData singleChoice = question(12, "Q10", "Rate",
                List.of(), List.of(new AnswerOptionData("Y", "Yes", 1), new AnswerOptionData("N", "No", 2)));

        return new SurveyStructure(SURVEY_ID, "en", List.of(
                new QuestionGroupData(1, "First group", "earlier", 1, List.of(multipleChoice, freeText)),
                new QuestionGroupData(2, "Second group", null, 2, List.of(singleChoice))));
    }

    private static QuestionData question(
            int questionId, String code, String text,
            List<SubQuestionData> subQuestions, List<AnswerOptionData> answerOptions) {
        return new QuestionData(questionId, 1, code, text, "T", questionId, false, subQuestions, answerOptions);
    }

    private static AnsweredQuestion question(SurveyResponse response, String code) {
        return response.groups().stream()
                .map(AnsweredGroup::questions)
                .flatMap(List::stream)
                .filter(question -> code.equals(question.code()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no question '" + code + "' in the response"));
    }

    /** Ordered map, so the matching is exercised with a predictable key order. */
    private static Map<String, Object> values(Object... keysAndValues) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            values.put(String.valueOf(keysAndValues[i]), keysAndValues[i + 1]);
        }
        return values;
    }

    private static org.assertj.core.groups.Tuple tuple3(String a, String b, Object c, String d) {
        return org.assertj.core.api.Assertions.tuple(a, b, c, d);
    }
}
