/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.api.v1.model.ParticipantDTO;
import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.*;
import io.redlink.more.studymanager.model.transformer.ParticipantTransformer;
import io.redlink.more.studymanager.properties.GatewayProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Service
public class ImportExportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportExportService.class);
    private final ParticipantService participantService;
    private final StudyService studyService;
    private final StudyStateService studyStateService;
    private final ObservationService observationService;
    private final InterventionService interventionService;
    private final StudyGroupService studyGroupService;
    private final IntegrationService integrationService;

    private final ElasticService elasticService;
    private final GatewayProperties gatewayProperties;

    public ImportExportService(ParticipantService participantService, StudyService studyService, StudyStateService studyStateService,
                               ObservationService observationService, InterventionService interventionService, StudyGroupService studyGroupService,
                               IntegrationService integrationService, ElasticService elasticService, GatewayProperties gatewayProperties) {
        this.participantService = participantService;
        this.studyService = studyService;
        this.studyStateService = studyStateService;
        this.observationService = observationService;
        this.interventionService = interventionService;
        this.studyGroupService = studyGroupService;
        this.integrationService = integrationService;
        this.elasticService = elasticService;
        this.gatewayProperties = gatewayProperties;
    }

    public Resource exportParticipants(Long studyId, User user) {
        List<ParticipantDTO> participantList = participantService.listParticipants(studyId)
                .stream()
                .map(participant -> ParticipantTransformer.toParticipantDTO_V1(participant, gatewayProperties))
                .toList();
        Study study = studyService.getStudy(studyId, user)
                .orElseThrow(() -> new NotFoundException("study", studyId));
        StringBuilder str = new StringBuilder("STUDYID;TITLE;ALIAS;PARTICIPANTID;REGISTRATIONTOKEN;REGISTRATIONURL\n");
        participantList.forEach(p -> str.append(writeToParticipantCsv(p, study)));
        return new ByteArrayResource(str.toString().getBytes(StandardCharsets.UTF_8));
    }

    public void importParticipants(Long studyId, InputStream inputStream) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        var scanner = new Scanner(inputStream).useDelimiter("[\\r\\n]+");
        boolean isHeader = true;
        while (scanner.hasNext()) {
            String line = scanner.next();
            if (!isHeader && StringUtils.isNotBlank(line)) {
                participantService.createParticipant(new Participant().setStudyId(studyId).setAlias(line));
            } else {
                isHeader = false;
            }
        }
        scanner.close();
    }

    private String writeToParticipantCsv(ParticipantDTO participant, Study study) {
        return "%d;%s;%s;%d;%s;%s\n".formatted(study.getStudyId(), study.getTitle(), participant.getAlias(),
                participant.getParticipantId(), participant.getRegistrationToken(), participant.getRegistrationUrl());
    }

    public StudyImportExport exportStudy(Long studyId, User user) {
        StudyImportExport export = new StudyImportExport()
                .setStudy(studyService.getStudy(studyId, user)
                        .orElseThrow(() -> new NotFoundException("study", studyId)))
                .setStudyGroups(studyGroupService.listStudyGroups(studyId))
                .setObservations(observationService.listObservations(studyId))
                .setInterventions(interventionService.listInterventions(studyId))
                .setActions(new HashMap<>())
                .setTriggers(new HashMap<>())
                .setParticipants(new ArrayList<>())
                .setIntegrations(new ArrayList<>());

        export.setParticipants(participantService.listParticipants(studyId)
                .stream()
                .sorted(Comparator.comparing(Participant::getParticipantId))
                .map(participant -> new StudyImportExport.ParticipantInfo(participant.getStudyGroupId()))
                .toList()
        );

        export.setIntegrations(
                export.getObservations().stream()
                        .map(Observation::getObservationId)
                        .flatMap(observationId -> {
                            List<EndpointToken> tokens = integrationService.getTokens(studyId, observationId);
                            return tokens.stream().map(token -> new IntegrationInfo(token.tokenLabel(), observationId));
                        }).toList()
        );

        for (Integer interventionId : export.getInterventions().stream().map(Intervention::getInterventionId).toList()) {
            export.getActions()
                    .put(interventionId, interventionService.listActions(studyId, interventionId));
            export.getTriggers()
                    .put(interventionId, interventionService.getTriggerByIds(studyId, interventionId));
        }
        return export;
    }

    @Transactional
    public Study importStudy(StudyImportExport studyImport, AuthenticatedUser user) {
        final Study newStudy = studyService.createStudy(studyImport.getStudy(), user);
        final Long studyId = newStudy.getStudyId();

        studyImport.getStudyGroups().forEach(studyGroup ->
                studyGroupService.importStudyGroup(studyId, studyGroup)
        );
        studyImport.getObservations().forEach(observation ->
                observationService.importObservation(studyId, observation)
        );
        studyImport.getInterventions().forEach(intervention ->
                interventionService.importIntervention(
                        studyId,
                        intervention,
                        studyImport.getTriggers().get(intervention.getInterventionId()),
                        studyImport.getActions().getOrDefault(intervention.getInterventionId(), Collections.emptyList())
                )
        );
        studyImport.getParticipants().forEach(participant ->
                participantService.createParticipant(
                        new Participant()
                                .setStudyId(studyId)
                                .setAlias("Participant")
                                .setStudyGroupId(participant.groupId())
                ));
        studyImport.getIntegrations().forEach(integration ->
                integrationService.addToken(studyId, integration.observationId(), integration.name())
        );

        return newStudy;
    }

    public void exportStudyData(OutputStream outputStream, Long studyId, List<Integer> studyGroupId, List<Integer> participantId, List<Integer> observationId, Instant from, Instant to) {
        if (studyService.existsStudy(studyId).orElse(false)) {
            exportStudyDataAsync(outputStream, studyId, studyGroupId, participantId, observationId, from, to);
        } else {
            throw NotFoundException.Study(studyId);
        }
    }

    @Async
    public void exportStudyDataAsync(OutputStream outputStream, Long studyId, List<Integer> studyGroupId, List<Integer> participantId, List<Integer> observationId, Instant from, Instant to) {
        try (outputStream) {
            outputStream.write("[".getBytes(StandardCharsets.UTF_8));
            elasticService.exportData(outputStream, studyId, studyGroupId, participantId, observationId, from, to);
            outputStream.write("]".getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            LOGGER.error("Cannot export study data for {}", studyId, e);
        }
    }
}
