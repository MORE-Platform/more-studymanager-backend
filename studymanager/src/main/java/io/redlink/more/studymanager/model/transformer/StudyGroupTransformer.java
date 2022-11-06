package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.StudyGroupDTO;
import io.redlink.more.studymanager.model.StudyGroup;

import java.time.OffsetDateTime;

public class StudyGroupTransformer {

    public static StudyGroup fromStudyGroupDTO_V1(StudyGroupDTO studyGroupDTO) {
        return new StudyGroup()
                .setStudyId(studyGroupDTO.getStudyId())
                .setStudyGroupId(studyGroupDTO.getStudyGroupId())
                .setTitle(studyGroupDTO.getTitle())
                .setPurpose(studyGroupDTO.getPurpose());
    }

    public static StudyGroupDTO toStudyGroupDTO_V1(StudyGroup studyGroup) {
        return new StudyGroupDTO()
                .studyId(studyGroup.getStudyId())
                .studyGroupId(studyGroup.getStudyGroupId())
                .title(studyGroup.getTitle())
                .purpose(studyGroup.getPurpose())
                .created(studyGroup.getCreated().toLocalDateTime().atOffset(OffsetDateTime.now().getOffset()))
                .modified(studyGroup.getModified().toLocalDateTime().atOffset(OffsetDateTime.now().getOffset()));
    }
}
