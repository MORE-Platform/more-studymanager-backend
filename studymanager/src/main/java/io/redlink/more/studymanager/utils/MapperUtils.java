/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.redlink.more.studymanager.exception.BadRequestException;

public class MapperUtils {
    public static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    public static <T> T readObject(Object o, Class<T> c) {
        if (o == null) return null;
        return MAPPER.convertValue(o, c);
    }

    public static <T> T readValue(Object o, Class<T> c) {
        if(o == null) return null;
        try {
            return MAPPER.readValue(o.toString(), c);
        } catch (JsonProcessingException e) {
            throw new BadRequestException(e);
        }
    }

    public static String writeValueAsString(Object o) {
        try {
            return MAPPER.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new BadRequestException(e);
        }
    }
}
