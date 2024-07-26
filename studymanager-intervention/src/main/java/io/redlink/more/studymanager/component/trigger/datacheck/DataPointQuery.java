/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.component.trigger.datacheck;

public class DataPointQuery {
    private int observationId;
    private String observationType;
    private String observationProperty;
    private String operator;
    private Object propertyValue;

    public DataPointQuery() {
    }

    public int getObservationId() {
        return observationId;
    }

    public String getObservationType() {
        return observationType;
    }

    public String getObservationProperty() {
        return observationProperty;
    }

    public String getOperator() {
        return operator;
    }

    public Object getPropertyValue() {
        return propertyValue;
    }

    public String toQueryString() {
        return "(observation_id.keyword:" + observationId +
                " AND " + getDataSelector() + ")";
    }

    private String getDataSelector() {
        if("=".equals(operator) || "==".equals(operator)) {
            return "data_" + observationProperty + ":" + getSanitizedPropertyValue();
        } else if("!=".equals(operator)) {
            return "NOT data_" + observationProperty + ":" + getSanitizedPropertyValue();
        } else if("<".equals(operator) || ">".equals(operator) || "<=".equals(operator) || ">=".equals(operator)) {
            return "data_" + observationProperty + ":" + operator + propertyValue;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private Object getSanitizedPropertyValue() {
        if(this.propertyValue instanceof String p) {
            return p.contains(" ") && !p.contains("\"") ?
                    "\"" + p.trim() + "\"" :
                    p.trim();
        }
        return this.propertyValue;
    }
}
