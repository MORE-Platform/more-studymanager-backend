package io.redlink.more.studymanager.model;

import java.time.LocalDate;

public record Timeframe (
    LocalDate from,
    LocalDate to
) {}
