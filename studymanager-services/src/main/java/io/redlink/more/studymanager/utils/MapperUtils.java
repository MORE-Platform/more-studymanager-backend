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
import com.google.common.io.CountingOutputStream;
import io.redlink.more.studymanager.exception.BadRequestException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

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

    /**
     * Validates if the given object can be serialized to JSON.
     * @param obj The object to validate.
     * @return true if serializable, false otherwise.
     */
    public static boolean isSerializable(Object obj) {
        return isSerializable(obj, -1);
    }

    /**
     * Validates if the given object can be serialized to JSON.
     * @param obj The object to validate.
     * @param byteLimit The limit of bytes allowed to serialize the parsed object.
     *                  Parse <code>%lt;= 0</code> for no limit.
     *                  NOTE that for primitive types and numbers the byte limit is ignored.
     * @return true if serializable, false otherwise.
     */
    public static boolean isSerializable(Object obj, long byteLimit) {
        //catch some commone cases
        if(obj == null) return true;
        Class<?> clazz = obj.getClass();
        if(clazz.isPrimitive() || clazz.isAssignableFrom(Number.class)) return true;
        //For String we want to consider the parsed byteLimit as this might include very long strings
        if(clazz.equals(String.class) && byteLimit > 0 &&
                (((String)obj).length() * 4L <= byteLimit || ((String)obj).getBytes(StandardCharsets.UTF_8).length <= byteLimit)) {
            return true;
        }
        //just try to serialize it
        try(CountingOutputStream out = new CountingOutputStream(new NullOutputStream())) {
            // Serialize to a no-op OutputStream to avoid producing unnecessary output
            MAPPER.writeValue(out, obj);
            return byteLimit <= 0 || out.getCount() <= byteLimit;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * No-op OutputStream that discards all data
     */
    private static class NullOutputStream extends OutputStream {
        @Override
        public void write(int b) {
            // Do nothing
        }

        @Override
        public void write(byte[] b, int off, int len) {
            // Do nothing
        }
    }
}
