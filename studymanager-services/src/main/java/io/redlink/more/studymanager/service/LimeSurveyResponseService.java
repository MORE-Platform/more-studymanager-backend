/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache License, Version 2.0.
 */
package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.component.observation.lime.LimeSurveyObservation;
import io.redlink.more.studymanager.component.observation.lime.LimeSurveyObservationFactory;
import io.redlink.more.studymanager.component.observation.lime.LimeSurveyRequestService;
import io.redlink.more.studymanager.component.observation.lime.model.AnswerOptionData;
import io.redlink.more.studymanager.component.observation.lime.model.QuestionData;
import io.redlink.more.studymanager.component.observation.lime.model.QuestionGroupData;
import io.redlink.more.studymanager.component.observation.lime.model.SubQuestionData;
import io.redlink.more.studymanager.component.observation.lime.model.SurveyStructure;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Assembles a participant's LimeSurvey submissions into a displayable structure: the survey's groups and
 * questions come from LimeSurvey, the answer values from the data points stored in Elasticsearch, and - when
 * nothing was stored for that participant - from LimeSurvey's response export as a fallback.
 */
@Service
public class LimeSurveyResponseService {

    private static final Logger LOG = LoggerFactory.getLogger(LimeSurveyResponseService.class);

    private static final DateTimeFormatter LIME_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String LIME_RESPONSE_ID_KEY = "id";
    private static final String LIME_SEED_KEY = "seed";
    private static final String LIME_SUBMIT_DATE_KEY = "submitdate";

    private final ObservationService observationService;
    private final ElasticDataService elasticDataService;
    private final MoreSDK sdk;
    private final ObjectProvider<LimeSurveyObservationFactory> limeSurveyObservationFactory;

    public LimeSurveyResponseService(
            ObservationService observationService,
            ElasticDataService elasticDataService,
            MoreSDK sdk,
            ObjectProvider<LimeSurveyObservationFactory> limeSurveyObservationFactory) {
        this.observationService = observationService;
        this.elasticDataService = elasticDataService;
        this.sdk = sdk;
        this.limeSurveyObservationFactory = limeSurveyObservationFactory;
    }

    /**
     * A participant's survey submissions for one LimeSurvey observation, newest first. Every group and
     * question of the survey is present in each response, even when it was not answered.
     *
     * @throws NotFoundException   if the observation does not exist in the study
     * @throws BadRequestException if the observation is not a LimeSurvey observation or has no survey assigned
     */
    public ParticipantSurveyResponses getParticipantResponses(
            long studyId, int observationId, int participantId, ResponseSelection selection) throws IOException {

        Observation observation = observationService.getObservation(studyId, observationId)
                .orElseThrow(() -> NotFoundException.Observation(studyId, observationId));

        if (!LimeSurveyObservationFactory.COMPONENT_ID.equals(observation.getType())) {
            throw new BadRequestException(
                    "Observation %d of study %d is of type '%s', not '%s'".formatted(
                            observationId, studyId, observation.getType(), LimeSurveyObservationFactory.COMPONENT_ID));
        }

        String surveyId = resolveSurveyId(observation)
                .orElseThrow(() -> new BadRequestException(
                        "Observation %d of study %d has no LimeSurvey assigned".formatted(observationId, studyId)));

        LimeSurveyRequestService requestService = limeSurveyObservationFactory.getObject().getRequestService();
        SurveyStructure structure = requestService.getSurveyStructure(surveyId);

        List<StoredSurveyResponse> stored = elasticDataService.listSurveyResponses(
                studyId, observation.getStudyGroupId(), observationId, participantId, selection);

        SurveyResponse.Source source = SurveyResponse.Source.STORED;
        if (stored.isEmpty()) {
            LOG.debug("No stored LimeSurvey data for participant {} of observation {}, falling back to LimeSurvey",
                    participantId, observationId);
            stored = fetchFromLimeSurvey(requestService, studyId, observationId, participantId, surveyId, selection);
            source = SurveyResponse.Source.LIME;
        }

        final SurveyResponse.Source responseSource = source;
        List<SurveyResponse> responses = stored.stream()
                .map(response -> toSurveyResponse(structure, response, responseSource))
                .sorted(NEWEST_FIRST)
                .toList();

        return new ParticipantSurveyResponses(
                studyId, observationId, participantId, surveyId, structure.language(), responses);
    }

    /**
     * The survey configured on the observation, falling back to the one stored when the observation was
     * activated - the same resolution order the observation component itself uses.
     */
    private Optional<String> resolveSurveyId(Observation observation) {
        String configured = observation.getProperties() == null
                ? null
                : observation.getProperties().getString(LimeSurveyObservation.LIME_SURVEY_ID);
        if (configured != null && !configured.isBlank()) {
            return Optional.of(configured);
        }
        return sdk.scopedObservationSDK(
                        observation.getStudyId(), observation.getStudyGroupId(), observation.getObservationId())
                .getValue(LimeSurveyObservation.LIME_SURVEY_ID, String.class)
                .filter(id -> !id.isBlank());
    }

    private List<StoredSurveyResponse> fetchFromLimeSurvey(
            LimeSurveyRequestService requestService,
            long studyId, int observationId, int participantId, String surveyId, ResponseSelection selection) {

        Optional<String> token = sdk.getPropertiesForParticipant(studyId, participantId, observationId)
                .map(properties -> properties.getString(LimeSurveyObservation.LIME_SURVEY_TOKEN_KEY))
                .filter(value -> !value.isBlank());

        if (token.isEmpty()) {
            LOG.info("Participant {} has no LimeSurvey token for observation {}", participantId, observationId);
            return List.of();
        }

        int numericSurveyId;
        try {
            numericSurveyId = Integer.parseInt(surveyId.trim());
        } catch (NumberFormatException e) {
            LOG.warn("LimeSurvey id '{}' of observation {} is not numeric", surveyId, observationId);
            return List.of();
        }

        return requestService.getAnswers(token.get(), numericSurveyId, selection).stream()
                .map(LimeSurveyResponseService::toStoredResponse)
                .toList();
    }

    private static StoredSurveyResponse toStoredResponse(Map<String, Object> answer) {
        return new StoredSurveyResponse(
                null,
                asInteger(answer.get(LIME_RESPONSE_ID_KEY)),
                asString(answer.get(LIME_SEED_KEY)),
                parseSubmitDate(answer.get(LIME_SUBMIT_DATE_KEY)),
                null,
                answer
        );
    }

    private SurveyResponse toSurveyResponse(
            SurveyStructure structure, StoredSurveyResponse response, SurveyResponse.Source source) {

        Map<String, List<Answer>> answersByQuestion = matchAnswers(structure, response.values());

        List<AnsweredGroup> groups = structure.groups().stream()
                .map(group -> toAnsweredGroup(group, answersByQuestion))
                .toList();

        Instant submitted = response.effectiveTimeFrame() != null
                ? response.effectiveTimeFrame()
                : parseSubmitDate(response.values().get(LIME_SUBMIT_DATE_KEY));

        return new SurveyResponse(response.responseId(), response.seed(), submitted, source, groups);
    }

    private AnsweredGroup toAnsweredGroup(QuestionGroupData group, Map<String, List<Answer>> answersByQuestion) {
        List<AnsweredQuestion> questions = group.questions().stream()
                .map(question -> new AnsweredQuestion(
                        question.questionId(),
                        question.code(),
                        question.text(),
                        question.type(),
                        question.mandatory(),
                        sortAnswers(question, answersByQuestion.getOrDefault(question.code(), List.of()))))
                .toList();
        return new AnsweredGroup(group.groupId(), group.title(), group.description(), questions);
    }

    /**
     * Assigns every stored value to the question it belongs to. A value's key is either the question code
     * itself, or {@code <questionCode>[<subQuestionCode>]} for the cells of array and multiple-choice
     * questions - which also covers LimeSurvey's {@code [other]} and {@code [comment]} cells.
     * <p>
     * Questions are tried longest code first, so a question {@code Q1} cannot claim the values of {@code Q10}.
     * Keys matching no question - LimeSurvey metadata such as {@code id}, {@code seed} or {@code lastpage} -
     * are ignored.
     */
    private Map<String, List<Answer>> matchAnswers(SurveyStructure structure, Map<String, Object> values) {
        List<QuestionData> questions = structure.groups().stream()
                .flatMap(group -> group.questions().stream())
                .filter(question -> question.code() != null && !question.code().isBlank())
                .sorted(Comparator.comparingInt((QuestionData question) -> question.code().length()).reversed())
                .toList();

        Map<String, List<Answer>> answersByQuestion = new HashMap<>();
        for (Map.Entry<String, Object> value : values.entrySet()) {
            String key = value.getKey();
            for (QuestionData question : questions) {
                String code = question.code();
                String subQuestionCode;
                if (key.equals(code)) {
                    subQuestionCode = null;
                } else if (key.startsWith(code + "[") && key.endsWith("]")) {
                    subQuestionCode = key.substring(code.length() + 1, key.length() - 1);
                } else {
                    continue;
                }
                answersByQuestion.computeIfAbsent(code, k -> new ArrayList<>())
                        .add(toAnswer(question, subQuestionCode, value.getValue()));
                break;
            }
        }
        return answersByQuestion;
    }

    private Answer toAnswer(QuestionData question, String subQuestionCode, Object value) {
        String subQuestionText = subQuestionCode == null ? null : question.subQuestions().stream()
                .filter(subQuestion -> subQuestionCode.equals(subQuestion.code()))
                .map(SubQuestionData::text)
                .findFirst()
                .orElse(null);

        return new Answer(subQuestionCode, subQuestionText, value, resolveLabel(question, value));
    }

    /** The answer option's text for a submitted code, or {@code null} for free text and unknown codes. */
    private String resolveLabel(QuestionData question, Object value) {
        if (value == null) {
            return null;
        }
        String code = String.valueOf(value);
        return question.answerOptions().stream()
                .filter(option -> code.equals(option.code()))
                .map(AnswerOptionData::label)
                .findFirst()
                .orElse(null);
    }

    /** Keeps the cells of an array question in the order LimeSurvey lists its subquestions. */
    private List<Answer> sortAnswers(QuestionData question, List<Answer> answers) {
        if (answers.size() < 2) {
            return answers;
        }
        List<String> order = question.subQuestions().stream().map(SubQuestionData::code).toList();
        return answers.stream()
                .sorted(Comparator.comparingInt(answer -> {
                    int index = answer.subQuestionCode() == null ? -1 : order.indexOf(answer.subQuestionCode());
                    return index < 0 ? Integer.MAX_VALUE : index;
                }))
                .toList();
    }

    private static final Comparator<SurveyResponse> NEWEST_FIRST = Comparator
            .comparing(SurveyResponse::responseId, Comparator.nullsFirst(Comparator.<Integer>naturalOrder()))
            .thenComparing(SurveyResponse::submitted, Comparator.nullsFirst(Comparator.<Instant>naturalOrder()))
            .reversed();

    /** LimeSurvey reports submit dates as {@code yyyy-MM-dd HH:mm:ss} without a zone; read them as UTC. */
    private static Instant parseSubmitDate(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(String.valueOf(value).trim(), LIME_DATE_FORMATTER).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            LOG.debug("Could not parse '{}' as a LimeSurvey submit date", value);
            return null;
        }
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static Integer asInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return value == null ? null : Integer.valueOf(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
