package io.redlink.more.studymanager.controller.converter;

import io.redlink.more.studymanager.api.v1.model.ParticipantDTO;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class CSVConverter extends AbstractHttpMessageConverter<List<ParticipantDTO>> {
    public final static MediaType TEXT_CSV_TYPE = new MediaType("text", "csv");

    @Autowired
    public CSVConverter() {
        super(StandardCharsets.UTF_8, TEXT_CSV_TYPE);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    protected List<ParticipantDTO> readInternal(Class<? extends List<ParticipantDTO>> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        List<String> participants = IOUtils.readLines(inputMessage.getBody(), inputMessage.getHeaders().getContentType().getCharset());
        participants.remove(0);
        return participants.stream().map(s -> new ParticipantDTO().alias(s)).toList();
    }

    @Override
    protected void writeInternal(List<ParticipantDTO> participantDTOS, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {

    }

}
