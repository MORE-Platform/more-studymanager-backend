package io.redlink.more.studymanager.component.observation.measurement;

import io.redlink.more.studymanager.core.measurement.Measurement;
import io.redlink.more.studymanager.core.measurement.MeasurementSet;

import java.util.Set;

public class GarminMeasurementSets {

    private GarminMeasurementSets() {
    }

    public static MeasurementSet ACTIVITY = new MeasurementSet(
            "ACTIVITY_END", Set.of(
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
            "SLEEP_END", Set.of(
            new Measurement("data_calendarDate", Measurement.Type.OBJECT), // array in the sample payload
            new Measurement("data_startTime", Measurement.Type.STRING),

            new Measurement("data_sleepScores", Measurement.Type.OBJECT),
            new Measurement("data_overallSleepScore", Measurement.Type.OBJECT),

            new Measurement("data_totalNapDurationInSeconds", Measurement.Type.LONG),
            new Measurement("data_unmeasurableSleepInSeconds", Measurement.Type.LONG),
            new Measurement("data_deepSleepDurationInSeconds", Measurement.Type.LONG),
            new Measurement("data_lightSleepDurationInSeconds", Measurement.Type.LONG),
            new Measurement("data_remSleepInSeconds", Measurement.Type.LONG),
            new Measurement("data_awakeDurationInSeconds", Measurement.Type.LONG),

            new Measurement("data_validation", Measurement.Type.STRING),

            new Measurement("data_naps", Measurement.Type.OBJECT),
            new Measurement("data_otherSleepData", Measurement.Type.OBJECT),
            new Measurement("data_timeOffsetSleepSpo2", Measurement.Type.OBJECT),

            new Measurement("data_summary_id", Measurement.Type.STRING)
    ));

    private static final Set<Measurement> STEP_MEASUREMENTS = Set.of(
            new Measurement("steps", Measurement.Type.INTEGER),
            new Measurement("stepsGoal", Measurement.Type.INTEGER),
            new Measurement("distanceInMeters", Measurement.Type.DOUBLE)
    );

    public static MeasurementSet DAILY_STEPS = new MeasurementSet(
            "DAILY_STEPS", STEP_MEASUREMENTS);

    public static MeasurementSet EPOCH_STEPS = new MeasurementSet(
            "EPOCH_STEPS", STEP_MEASUREMENTS);
}
