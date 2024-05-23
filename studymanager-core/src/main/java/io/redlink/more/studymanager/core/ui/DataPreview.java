package io.redlink.more.studymanager.core.ui;

public class DataPreview {
    private String chartTitle;
    private String chartType;

    public DataPreview(String chartTitle, String chartType) {
        this.chartTitle = chartTitle;
        this.chartType = chartType;
    }

    public String getChartTitle() {
        return chartTitle;
    }

    public String getChartType() {
        return chartType;
    }
}
