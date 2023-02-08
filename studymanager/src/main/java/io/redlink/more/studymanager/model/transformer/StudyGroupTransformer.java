package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.StudyGroupDTO;
import io.redlink.more.studymanager.model.StudyGroup;

public final class StudyGroupTransformer {

    private StudyGroupTransformer() {
    }

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
                .created(Transformers.toOffsetDateTime(studyGroup.getCreated()))
                .modified(Transformers.toOffsetDateTime(studyGroup.getModified()));
    }
}
