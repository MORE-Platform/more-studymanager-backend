/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.controller.converter;
import io.redlink.more.studymanager.api.v1.model.ParticipantDTO;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;
@Component
public class CSVConverter extends AbstractGenericHttpMessageConverter<List<ParticipantDTO>> {
    public static final MediaType TEXT_CSV_TYPE = new MediaType("text", "csv");
    @Autowired
    public CSVConverter() {
        super(TEXT_CSV_TYPE);
        setDefaultCharset(StandardCharsets.UTF_8);
    }
    @Override
    protected boolean supports(Class<?> clazz) {
        return List.class.isAssignableFrom(clazz);
    }
    @Override
    public boolean canWrite(Type type, Class<?> clazz, MediaType mediaType) {
        return false;
    }
    @Override
    protected void writeInternal(List<ParticipantDTO> participantDTOS, Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        throw new HttpMessageNotWritableException("Writing not supported");
    }
    @Override
    protected List<ParticipantDTO> readInternal(Class<? extends List<ParticipantDTO>> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return readParticipantsDTO(inputMessage);
    }
    @Override
    public List<ParticipantDTO> read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return readParticipantsDTO(inputMessage);
    }
    private List<ParticipantDTO> readParticipantsDTO(HttpInputMessage inputMessage) throws IOException {
        var charset = Optional.ofNullable(inputMessage.getHeaders().getContentType())
                .map(MediaType::getCharset)
                .or(() -> Optional.ofNullable(this.getDefaultCharset()))
                .orElse(StandardCharsets.UTF_8);
        return IOUtils.readLines(inputMessage.getBody(), charset)
                .stream()
                .skip(1)
                .map(alias -> new ParticipantDTO().alias(alias))
                .toList();
    }
}
