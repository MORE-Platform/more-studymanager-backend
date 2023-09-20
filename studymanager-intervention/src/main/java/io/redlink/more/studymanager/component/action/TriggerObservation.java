package io.redlink.more.studymanager.component.action;

public class TriggerObservation {
    private Integer id;
    private String factory;

    public Integer getId() {
        return id;
    }

    public TriggerObservation setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getFactory() {
        return factory;
    }

    public TriggerObservation setFactory(String factory) {
        this.factory = factory;
        return this;
    }
}
