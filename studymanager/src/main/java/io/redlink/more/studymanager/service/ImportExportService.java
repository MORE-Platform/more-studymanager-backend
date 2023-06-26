package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.*;
import jakarta.servlet.ServletOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

@Service
public class ImportExportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportExportService.class);
    private final ParticipantService participantService;
    private final StudyService studyService;
    private final StudyStateService studyStateService;
    private final ObservationService observationService;
    private final InterventionService interventionService;
    private final StudyGroupService studyGroupService;

    private final ElasticService elasticService;

    public ImportExportService(ParticipantService participantService, StudyService studyService, StudyStateService studyStateService,
                               ObservationService observationService, InterventionService interventionService, StudyGroupService studyGroupService, ElasticService elasticService) {
        this.participantService = participantService;
        this.studyService = studyService;
        this.studyStateService = studyStateService;
        this.observationService = observationService;
        this.interventionService = interventionService;
        this.studyGroupService = studyGroupService;
        this.elasticService = elasticService;
    }

    public Resource exportParticipants(Long studyId, User user) {
        List<Participant> participantList = participantService.listParticipants(studyId);
        Study study = studyService.getStudy(studyId, user)
                .orElseThrow(() -> new NotFoundException("study", studyId));
        StringBuilder str = new StringBuilder("STUDYID;TITLE;ALIAS;PARTICIPANTID;REGISTRATIONTOKEN\n");
        participantList.forEach(p -> str.append(writeToParticipantCsv(p, study)));
        return new ByteArrayResource(str.toString().getBytes(StandardCharsets.UTF_8));
    }

    public void importParticipants(Long studyId, InputStream inputStream) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        var scanner = new Scanner(inputStream).useDelimiter("[\\r\\n]+");
        boolean isHeader = true;
        while (scanner.hasNext()) {
            String line = scanner.next();
            if(!isHeader && StringUtils.isNotBlank(line)) {
                participantService.createParticipant(new Participant().setStudyId(studyId).setAlias(line));
            } else {
                isHeader = false;
            }
        }
        scanner.close();
    }

    private String writeToParticipantCsv(Participant participant, Study study) {
        return "%d;%s;%s;%d;%s\n".formatted(study.getStudyId(), study.getTitle(), participant.getAlias(),
                participant.getParticipantId(), participant.getRegistrationToken());
    }

    public StudyImportExport exportStudy(Long studyId, User user) {
        StudyImportExport export = new StudyImportExport()
                .setStudy(studyService.getStudy(studyId, user)
                        .orElseThrow(() -> new NotFoundException("study", studyId)))
                .setStudyGroups(studyGroupService.listStudyGroups(studyId))
                .setObservations(observationService.listObservations(studyId))
                .setInterventions(interventionService.listInterventions(studyId))
                .setActions(new HashMap<>())
                .setTriggers(new HashMap<>());

        for(Integer interventionId: export.getInterventions().stream().map(Intervention::getInterventionId).toList()) {
            export.getActions()
                    .put(interventionId, interventionService.listActions(studyId, interventionId));
            export.getTriggers()
                    .put(interventionId, interventionService.getTriggerByIds(studyId, interventionId));
        }
        return export;
    }

    public Study importStudy(StudyImportExport studyImport, AuthenticatedUser user) {
        Study newStudy = studyService.createStudy(studyImport.getStudy(), user);
        Long studyId = newStudy.getStudyId();
        HashMap<Integer,Integer> studyGroupIds = new HashMap<>();
        HashMap<Integer,Integer> interventionIds = new HashMap<>();

        studyImport.getStudyGroups().forEach(studyGroup ->
                studyGroupIds.put(
                        studyGroup.getStudyGroupId(),
                        studyGroupService.createStudyGroup(studyGroup.setStudyId(studyId)).getStudyGroupId())
        );
        studyImport.getObservations().forEach(observation -> {
            if(observation.getStudyGroupId() != null) {
                observation.setStudyGroupId(studyGroupIds.get(observation.getStudyGroupId()));
            }
            observationService.addObservation(observation.setStudyId(studyId));
        });
        studyImport.getInterventions().forEach(intervention -> {
            if(intervention.getStudyGroupId() != null) {
                intervention.setStudyGroupId(studyGroupIds.get(intervention.getStudyGroupId()));
            }
            interventionIds.put(
                    intervention.getInterventionId(),
                    interventionService.addIntervention(intervention.setStudyId(studyId)).getInterventionId());
        });
        studyImport.getTriggers().forEach((oldInterventionId, trigger) ->
            interventionService.updateTrigger(studyId, interventionIds.get(oldInterventionId), trigger)
        );
        studyImport.getActions().forEach((oldInterventionId, actionList) ->
            actionList.forEach(action ->
                    interventionService.createAction(studyId, interventionIds.get(oldInterventionId), action))
        );
        return newStudy;
    }

    public void exportStudyData(ServletOutputStream outputStream, Long studyId, AuthenticatedUser currentUser) {
        studyService.getStudy(studyId, currentUser).ifPresent(s -> exportStudyDataAsync(outputStream, studyId));
    }

    @Async
    public void exportStudyDataAsync(ServletOutputStream outputStream, Long studyId) {
        try(outputStream) {
            outputStream.write("[".getBytes(StandardCharsets.UTF_8));
            elasticService.exportData(studyId, outputStream);
            outputStream.write("]".getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            LOGGER.error("Cannot export study data for {}", studyId, e);
        }
    }
}
