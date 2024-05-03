package io.redlink.more.studymanager.model.timeline;

import java.util.ArrayList;
import java.util.List;

public class StudyTimeline {
    List<ObservationTimelineEvent> observationTimelineEvents;
    List<InterventionTimelineEvent> interventionTimelineEvents;

    public StudyTimeline() {
        observationTimelineEvents = new ArrayList<>();
        interventionTimelineEvents = new ArrayList<>();
    }

    public void addObservationTimelineEvent(ObservationTimelineEvent event) {
        observationTimelineEvents.add(event);
    }

    public void addAllObservations(List<ObservationTimelineEvent> events) { observationTimelineEvents.addAll(events); }

    public List<ObservationTimelineEvent> getObservationTimelineEvents() {
        return observationTimelineEvents;
    }

    public void addInterventionTimelineEvent(InterventionTimelineEvent event) {
        interventionTimelineEvents.add(event);
    }

    public void addAllInterventions(List<InterventionTimelineEvent> events) { interventionTimelineEvents.addAll(events); }

    public List<InterventionTimelineEvent> getInterventionTimelineEvents() {
        return interventionTimelineEvents;
    }
}
