package io.redlink.more.studymanager.core.datavalidity;

import io.redlink.more.studymanager.core.measurement.Measurement;

public class MeasurementSummary {
    private final Measurement measurement;
    private NumericMeasurementSummary numericResult;
    private StringMeasurementSummary stringResult;
    private BooleanMeasurementSummary booleanResult;
    private DateMeasurementSummary dateResult;

    public MeasurementSummary(Measurement measurement) {
        this.measurement = measurement;
    }

    public void setNumericResult(NumericMeasurementSummary numericResult) {
        this.numericResult = numericResult;
    }

    public void setStringResult(StringMeasurementSummary stringResult) {
        this.stringResult = stringResult;
    }

    public void setBooleanResult(BooleanMeasurementSummary booleanResult) {
        this.booleanResult = booleanResult;
    }

    public void setDateResult(DateMeasurementSummary dateResult) {
        this.dateResult = dateResult;
    }

    /**
     * Getter for the Measurement
     * @return
     */
    public Measurement getMeasurement() {
        return measurement;
    }

    /**
     * Getter for information about numeric values. Only present
     * of {@link Measurement#getType()} is {@link Measurement.Type#DOUBLE} or {@link Measurement.Type#INTEGER}
     * @return
     */
    public NumericMeasurementSummary getNumericResult() {
        return numericResult;
    }

    /**
     * Getter for information about string values. Only present
     * of {@link Measurement#getType()} is {@link Measurement.Type#STRING}}
     * @return
     */
    public StringMeasurementSummary getStringResult() {
        return stringResult;
    }

    /**
     * Getter for information about boolean values. Only present
     * of {@link Measurement#getType()} is {@link Measurement.Type#BOOLEAN}}
     * @return
     */
    public BooleanMeasurementSummary getBooleanResult() {
        return booleanResult;
    }

    /**
     * Getter for information about date values. Only present
     * of {@link Measurement#getType()} is {@link Measurement.Type#DATE}}
     * @return
     */
    public DateMeasurementSummary getDateResult() {
        return dateResult;
    }

}