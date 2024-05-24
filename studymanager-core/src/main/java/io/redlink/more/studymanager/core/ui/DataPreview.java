package io.redlink.more.studymanager.core.ui;

import java.util.Set;

public class DataPreview {
    private String chartTitle;
    private String chartType;
    private Set<String> measurements;

    public DataPreview(String chartTitle, String chartType, String... measurements) {
        this.chartTitle = chartTitle;
        this.chartType = chartType;
        this.measurements = Set.of(measurements);
    }

    public String getChartTitle() {
        return chartTitle;
    }

    public String getChartType() {
        return chartType;
    }

    public Set<String> getMeasurements() { return measurements; }
}
