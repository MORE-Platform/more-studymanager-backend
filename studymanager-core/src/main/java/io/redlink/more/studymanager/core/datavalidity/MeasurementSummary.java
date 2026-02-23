package io.redlink.more.studymanager.core.datavalidity;

import io.redlink.more.studymanager.core.measurement.Measurement;

import java.util.Objects;

public class MeasurementSummary {
    private final Measurement measurement;
    private NumericMeasurementSummary numericResult;
    private StringMeasurementSummary stringResult;
    private BooleanMeasurementSummary booleanResult;
    private DateMeasurementSummary dateResult;
    private StringArrayMeasurementSummary arrayResult;

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
     *
     * @return
     */
    public Measurement getMeasurement() {
        return measurement;
    }

    /**
     * Getter for information about numeric values. Only present
     * of {@link Measurement#getType()} is {@link Measurement.Type#DOUBLE} or {@link Measurement.Type#INTEGER}
     *
     * @return
     */
    public NumericMeasurementSummary getNumericResult() {
        return numericResult;
    }

    /**
     * Getter for information about string values. Only present
     * of {@link Measurement#getType()} is {@link Measurement.Type#STRING}}
     *
     * @return
     */
    public StringMeasurementSummary getStringResult() {
        return stringResult;
    }

    /**
     * Getter for information about boolean values. Only present
     * of {@link Measurement#getType()} is {@link Measurement.Type#BOOLEAN}}
     *
     * @return
     */
    public BooleanMeasurementSummary getBooleanResult() {
        return booleanResult;
    }

    /**
     * Getter for information about date values. Only present
     * of {@link Measurement#getType()} is {@link Measurement.Type#DATE}}
     *
     * @return
     */
    public DateMeasurementSummary getDateResult() {
        return dateResult;
    }

    public StringArrayMeasurementSummary getArrayResult() {
        return arrayResult;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MeasurementSummary that = (MeasurementSummary) o;
        return Objects.equals(measurement, that.measurement) && Objects.equals(numericResult, that.numericResult) && Objects.equals(stringResult, that.stringResult) && Objects.equals(booleanResult, that.booleanResult) && Objects.equals(dateResult, that.dateResult);
    }

    @Override
    public int hashCode() {
        return Objects.hash(measurement, numericResult, stringResult, booleanResult, dateResult);
    }

    @Override
    public String toString() {
        return "MeasurementSummary{" +
                "measurement=" + measurement +
                ", numericResult=" + numericResult +
                ", stringResult=" + stringResult +
                ", booleanResult=" + booleanResult +
                ", dateResult=" + dateResult +
                ", arrayResult=" + arrayResult +
                '}';
    }
}