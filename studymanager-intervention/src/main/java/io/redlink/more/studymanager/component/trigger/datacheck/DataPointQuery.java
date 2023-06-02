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
            return "data_" + observationProperty + ":" + propertyValue;
        } else if("!=".equals(operator)) {
            return "!(data_" + observationProperty + ":" + propertyValue + ")";
        } else if("<".equals(operator) || ">".equals(operator) || "<=".equals(operator) || ">=".equals(operator)) {
            return "data_" + observationProperty + ":" + operator + propertyValue;
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
