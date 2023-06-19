package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.ContactDTO;
import io.redlink.more.studymanager.api.v1.model.StatusChangeDTO;
import io.redlink.more.studymanager.api.v1.model.StudyDTO;
import io.redlink.more.studymanager.api.v1.model.StudyStatusDTO;
import io.redlink.more.studymanager.model.Contact;
import io.redlink.more.studymanager.model.Study;

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
                .setParticipantInfo(studyDTO.getParticipantInfo())
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
                .participantInfo(study.getParticipantInfo())
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
