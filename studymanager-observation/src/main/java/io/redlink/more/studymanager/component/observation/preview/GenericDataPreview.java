package io.redlink.more.studymanager.component.observation.preview;

import io.redlink.more.studymanager.core.ui.DataPreview;

public class GenericDataPreview {

    private GenericDataPreview() {}

    public static final DataPreview HEART_RATE = new DataPreview("Heartrate", "line");
    public static final DataPreview QUESTION = new DataPreview("Simple Questionnaire", "line");
    public static final DataPreview GPS = new DataPreview("GPS", "gps");
    public static final DataPreview ACCELEROMETER = new DataPreview("Accelerometer", "bubble");
    public static final DataPreview NOT_SPECIFIED = new DataPreview(null, null);
}
