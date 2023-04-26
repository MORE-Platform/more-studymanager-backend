/*
 * Copyright (c) 2023 Redlink GmbH.
 */
package io.redlink.more.studymanager.utils;

import io.redlink.more.studymanager.model.Action;
import io.redlink.more.studymanager.model.Intervention;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.StudyGroup;
import java.util.Map;
import org.slf4j.MDC;

public final class LoggingUtils {

    private LoggingUtils() {
    }

    public static LoggingContext createContext() {
        return new LoggingContext();
    }

    public static LoggingContext createContext(Study study) {
        final LoggingContext ctx = new LoggingContext();
        ctx.putStudy(study);
        return ctx;
    }

    public static class LoggingContext implements AutoCloseable {

        public static final String MDC_KEY_STUDY = "studyId";
        public static final String MDC_KEY_PARTICIPANT = "participantId";
        public static final String MDC_KEY_ACTION = "actionId";
        public static final String MDC_KEY_ACTION_TYPE = "actionType";
        public static final String MDC_KEY_STUDYGROUP = "studyGroupId";
        public static final String MDC_KEY_INTERVENTION = "interventionId";

        private final Map<String, String> oldMap;

        public LoggingContext() {
            oldMap = MDC.getCopyOfContextMap();
        }

        public void putStudy(Study study) {
            putStudy(study.getStudyId());
        }

        public void putStudy(Long study) {
            MDC.put(MDC_KEY_STUDY, String.valueOf(study));
        }

        public void putParticipant(Participant participant) {
            putStudy(participant.getStudyId());
            putParticipant(participant.getParticipantId());
        }

        public void putParticipant(Integer participantId) {
            MDC.put(MDC_KEY_PARTICIPANT, String.valueOf(participantId));
        }

        public void putStudyGroup(StudyGroup studyGroup) {
            putStudy(studyGroup.getStudyId());
            putStudyGroup(studyGroup.getStudyGroupId());
        }

        public void putStudyGroup(Integer studyGroupId) {
            MDC.put(MDC_KEY_STUDYGROUP, String.valueOf(studyGroupId));
        }

        public void putAction(Action action) {
            putAction(action.getActionId(), action.getType());
        }

        public void putAction(Integer actionId, String actionType) {
            MDC.put(MDC_KEY_ACTION, String.valueOf(actionId));
            MDC.put(MDC_KEY_ACTION_TYPE, actionType);
        }

        public void putIntervention(Intervention intervention) {
            putIntervention(intervention.getInterventionId());
        }

        public void putIntervention(Integer interventionId) {
            MDC.put(MDC_KEY_INTERVENTION, String.valueOf(interventionId));
        }

        @Override
        public void close() {
            if (oldMap != null) {
                MDC.setContextMap(oldMap);
            } else {
                MDC.clear();
            }
        }
    }

}
