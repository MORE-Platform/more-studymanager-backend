package io.redlink.more.studymanager.model.data;

import java.util.List;

public record MonitoringData(
        String chartTitle,
        String chartType,
        List<DataRow> dataRows
) {
    public void addDataRow(DataRow row) {
        dataRows.add(row);
    }

    public record DataRow(
            String rowTitle,
            List<List<Object>> data
    ) {}
}
