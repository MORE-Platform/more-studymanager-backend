package io.redlink.more.studymanager.component.observation.measurement;

import io.redlink.more.studymanager.core.measurement.Measurement;
import io.redlink.more.studymanager.core.measurement.MeasurementSet;

import java.util.Set;

public class GarminMeasurementSets {

    private GarminMeasurementSets() {
    }

    public static MeasurementSet ACTIVITY = new MeasurementSet(
            "ACTIVITY", Set.of(
            new Measurement("activityType", Measurement.Type.STRING),
            new Measurement("met", Measurement.Type.DOUBLE),
            new Measurement("intensity", Measurement.Type.STRING),
            new Measurement("activeTimeInSeconds", Measurement.Type.LONG),
            new Measurement("meanMotionIntensity", Measurement.Type.DOUBLE),
            new Measurement("maxMotionIntensity", Measurement.Type.DOUBLE)
    ));

    public static MeasurementSet BLOOD_PRESSURE = new MeasurementSet(
            "BLOOD_PRESSURE", Set.of(
            new Measurement("systolic", Measurement.Type.INTEGER),
            new Measurement("diastolic", Measurement.Type.INTEGER),
            new Measurement("pulse", Measurement.Type.INTEGER),
            new Measurement("sourceType", Measurement.Type.STRING)
    ));

    public static MeasurementSet SLEEP = new MeasurementSet(
            "SLEEP", Set.of(
            new Measurement("calendarDate", Measurement.Type.STRING),
            new Measurement("totalNapDurationInSeconds", Measurement.Type.LONG),
            new Measurement("unmeasurableSleepInSeconds", Measurement.Type.LONG),
            new Measurement("deepSleepDurationInSeconds", Measurement.Type.LONG),
            new Measurement("lightSleepDurationInSeconds", Measurement.Type.LONG),
            new Measurement("remSleepInSeconds", Measurement.Type.LONG),
            new Measurement("awakeDurationInSeconds", Measurement.Type.LONG),
            new Measurement("validation", Measurement.Type.STRING),

            new Measurement("overallSleepScoreValue", Measurement.Type.INTEGER),
            new Measurement("overallSleepScoreQualifierKey", Measurement.Type.STRING),

            new Measurement("sleepScores_totalDuration_qualifierKey", Measurement.Type.STRING),
            new Measurement("sleepScores_stress_qualifierKey", Measurement.Type.STRING),
            new Measurement("sleepScores_awakeCount_qualifierKey", Measurement.Type.STRING),
            new Measurement("sleepScores_remPercentage_qualifierKey", Measurement.Type.STRING),
            new Measurement("sleepScores_restlessness_qualifierKey", Measurement.Type.STRING),
            new Measurement("sleepScores_lightPercentage_qualifierKey", Measurement.Type.STRING),
            new Measurement("sleepScores_deepPercentage_qualifierKey", Measurement.Type.STRING)
    ));

    public static MeasurementSet STEPS = new MeasurementSet(
            "STEPS", Set.of(
            new Measurement("steps", Measurement.Type.INTEGER),
            new Measurement("stepsGoal", Measurement.Type.INTEGER),
            new Measurement("distanceInMeters", Measurement.Type.DOUBLE)
    ));
}
