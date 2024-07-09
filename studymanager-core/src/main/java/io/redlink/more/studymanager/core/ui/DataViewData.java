package io.redlink.more.studymanager.core.ui;

import java.util.List;

public record DataViewData(
        List<String> labels,
        List<DataViewRow> rows
) {
}
