/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.StudyGroupDTO;
import io.redlink.more.studymanager.model.StudyGroup;
import io.redlink.more.studymanager.model.scheduler.Duration;
import java.time.Instant;

public final class StudyGroupTransformer {

    private StudyGroupTransformer() {
    }

    public static StudyGroup fromStudyGroupDTO_V1(StudyGroupDTO studyGroupDTO) {
        return new StudyGroup()
                .setStudyId(studyGroupDTO.getStudyId())
                .setStudyGroupId(studyGroupDTO.getStudyGroupId())
                .setTitle(studyGroupDTO.getTitle())
                .setPurpose(studyGroupDTO.getPurpose())
                .setDuration(Duration.fromStudyDurationDTO(studyGroupDTO.getDuration()));
    }

    public static StudyGroupDTO toStudyGroupDTO_V1(StudyGroup studyGroup) {
        Instant instant = studyGroup.getModified();
        Instant instant1 = studyGroup.getCreated();
        return new StudyGroupDTO()
                .studyId(studyGroup.getStudyId())
                .studyGroupId(studyGroup.getStudyGroupId())
                .title(studyGroup.getTitle())
                .purpose(studyGroup.getPurpose())
                .duration(Duration.toStudyDurationDTO(studyGroup.getDuration()))
                .created(instant1)
                .modified(instant);
    }
}
