/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.*;
import io.redlink.more.studymanager.model.Contact;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.scheduler.Duration;

import java.util.Optional;

public class StudyTransformer {

    private StudyTransformer() {}

    public static Study fromStudyDTO_V1(StudyDTO studyDTO) {
        //NOTE: status, start, end, created and modified are never set directly but by status update
        //TODO some assertions? which throws validation exception
        if(studyDTO.getContact() == null)
            studyDTO.contact(new ContactDTO());
        return new Study()
                .setStudyId(studyDTO.getStudyId())
                .setTitle(studyDTO.getTitle())
                .setPurpose(studyDTO.getPurpose())
                .setFinishText(studyDTO.getFinishText())
                .setParticipantInfo(studyDTO.getParticipantInfo())
                .setDuration(Duration.fromStudyDurationDTO(studyDTO.getDuration()))
                .setConsentInfo(studyDTO.getConsentInfo())
                .setPlannedStartDate(studyDTO.getPlannedStart())
                .setPlannedEndDate(studyDTO.getPlannedEnd())
                .setContact(ContactTransformer.fromContactDTO_V1(studyDTO.getContact()));
    }

    public static StudyDTO toStudyDTO_V1(Study study) {
        if(study.getContact() == null)
            study.setContact(new Contact());
        return new StudyDTO()
                .studyId(study.getStudyId())
                .title(study.getTitle())
                .purpose(study.getPurpose())
                .finishText(study.getFinishText())
                .participantInfo(study.getParticipantInfo())
                .duration(Duration.toStudyDurationDTO(study.getDuration()))
                .consentInfo(study.getConsentInfo())
                .status(StudyStatusDTO.fromValue(study.getStudyState().getValue()))
                .start(study.getStartDate())
                .end(study.getEndDate())
                .plannedStart(study.getPlannedStartDate())
                .plannedEnd(study.getPlannedEndDate())
                .created(Transformers.toOffsetDateTime(study.getCreated()))
                .modified(Transformers.toOffsetDateTime(study.getModified()))
                .userRoles(RoleTransformer.toStudyRolesDTO(study.getUserRoles()))
                .contact(ContactTransformer.toContactDTO_V1(study.getContact()));
    }

    public static Study.Status fromStatusChangeDTO_V1(StatusChangeDTO statusChangeDTO) {
        return Study.Status.valueOf(statusChangeDTO.getStatus().getValue().toUpperCase());
    }
}
