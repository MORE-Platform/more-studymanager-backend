package io.redlink.more.studymanager.component.observation.preview;

import io.redlink.more.studymanager.core.ui.DataPreview;

public class GenericDataPreview {

    private GenericDataPreview() {}

    public static final DataPreview HEART_RATE = new DataPreview("Heartrate", "line", "hr");
    public static final DataPreview QUESTION = new DataPreview("Simple Questionnaire", "line", "NOT_SPECIFIED");
    public static final DataPreview ACCELEROMETER = new DataPreview("Accelerometer", "bubble", "x", "y", "z");
    public static final DataPreview NOT_SPECIFIED = new DataPreview("NOT_SPECIFIED", "NOT_SPECIFIED", "NOT_SPECIFIED");
}
