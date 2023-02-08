package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.StatusChangeDTO;
import io.redlink.more.studymanager.api.v1.model.StudyDTO;
import io.redlink.more.studymanager.api.v1.model.StudyStatusDTO;
import io.redlink.more.studymanager.model.Study;
import java.sql.Date;

public class StudyTransformer {

    private StudyTransformer() {}

    public static Study fromStudyDTO_V1(StudyDTO studyDTO) {
        //NOTE: status, start, end, created and modified are never set directly but by status update
        //TODO some assertions? which throws validation exception
        return new Study()
                .setStudyId(studyDTO.getStudyId())
                .setTitle(studyDTO.getTitle())
                .setPurpose(studyDTO.getPurpose())
                .setParticipantInfo(studyDTO.getParticipantInfo())
                .setConsentInfo(studyDTO.getConsentInfo())
                .setPlannedStartDate(Transformers.toSqlDate(studyDTO.getPlannedStart()))
                .setPlannedEndDate(Transformers.toSqlDate(studyDTO.getPlannedEnd()));
    }

    public static StudyDTO toStudyDTO_V1(Study study) {
        return new StudyDTO()
                .studyId(study.getStudyId())
                .title(study.getTitle())
                .purpose(study.getPurpose())
                .participantInfo(study.getParticipantInfo())
                .consentInfo(study.getConsentInfo())
                .status(StudyStatusDTO.fromValue(study.getStudyState().getValue()))
                .start(Transformers.toLocalDate(study.getStartDate()))
                .end(Transformers.toLocalDate(study.getEndDate()))
                .plannedStart(Transformers.toLocalDate(study.getPlannedStartDate()))
                .plannedEnd(Transformers.toLocalDate(study.getPlannedEndDate()))
                .created(Transformers.toOffsetDateTime(study.getCreated()))
                .modified(Transformers.toOffsetDateTime(study.getModified()))
                .userRoles(RoleTransformer.toStudyRolesDTO(study.getUserRoles()))
                ;
    }

    public static Study.Status fromStatusChangeDTO_V1(StatusChangeDTO statusChangeDTO) {
        return Study.Status.valueOf(statusChangeDTO.getStatus().getValue().toUpperCase());
    }
}
