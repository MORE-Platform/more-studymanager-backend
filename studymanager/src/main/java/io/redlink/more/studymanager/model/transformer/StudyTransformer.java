package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.StatusChangeDTO;
import io.redlink.more.studymanager.api.v1.model.StudyDTO;
import io.redlink.more.studymanager.api.v1.model.StudyStatusDTO;
import io.redlink.more.studymanager.model.Study;

import java.sql.Date;
import java.time.OffsetTime;

public class StudyTransformer {

    private StudyTransformer() {}

    public static Study fromStudyDTO_V1(StudyDTO studyDTO) {
        //NOTE: status, start, end, created and modified are never set directly but by status update
        //TODO some assertions? which throws validation exception
        return new Study()
                .setStudyId(studyDTO.getStudyId() != null ? studyDTO.getStudyId() : null)
                .setTitle(studyDTO.getTitle())
                .setPurpose(studyDTO.getPurpose())
                .setParticipantInfo(studyDTO.getParticipantInfo())
                .setConsentInfo(studyDTO.getConsentInfo())
                .setPlannedStartDate(Date.valueOf(studyDTO.getPlannedStart()))
                .setPlannedEndDate(Date.valueOf(studyDTO.getPlannedEnd()));
    }

    public static StudyDTO toStudyDTO_V1(Study study) {
        return new StudyDTO()
                .studyId(study.getStudyId())
                .title(study.getTitle())
                .purpose(study.getPurpose())
                .participantInfo(study.getParticipantInfo())
                .consentInfo(study.getConsentInfo())
                .status(StudyStatusDTO.fromValue(study.getStudyState().getValue()))
                .start(study.getStartDate() != null ? study.getStartDate().toLocalDate() : null)
                .end(study.getEndDate() != null ? study.getEndDate().toLocalDate() : null)
                .plannedStart(study.getPlannedStartDate().toLocalDate())
                .plannedEnd(study.getPlannedEndDate().toLocalDate())
                .created(study.getCreated().toLocalDate().atTime(OffsetTime.now()))
                .modified(study.getModified().toLocalDate().atTime(OffsetTime.now()));
    }

    public static Study.Status fromStatusChangeDTO_V1(StatusChangeDTO statusChangeDTO) {
        return Study.Status.valueOf(statusChangeDTO.getStatus().getValue().toUpperCase());
    }
}
