package io.redlink.more.studymanager.core.datavalidity;

public record FieldValue<T>(
        T value,
        long count
) {
}
