package io.redlink.more.studymanager.service;

import com.fasterxml.jackson.core.JsonEncoding;
import io.redlink.more.studymanager.model.audit.AuditLog;
import io.redlink.more.studymanager.properties.AuditProperties;
import io.redlink.more.studymanager.repository.AuditLogRepository;
import io.redlink.more.studymanager.utils.MapperUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@EnableConfigurationProperties(AuditProperties.class)
public class AuditService {

    private final AuditProperties auditProperties;
    private final AuditLogRepository auditLogRepository;
    private final StudyStateService studystateService;
    AuditService(
            AuditProperties auditProperties,
            AuditLogRepository auditLogRepository,
            StudyStateService studystateService
    ) {
        this.auditProperties = auditProperties;
        this.auditLogRepository = auditLogRepository;
        this.studystateService = studystateService;
    }

    /**
     * Prepare the auditlog for streaming it to the export
     * @param outputStream outputStream for exported auditlog entries
     * @param studyId corresponding studyId of the auditlogs
     */
    public void exportAuditlog(OutputStream outputStream, Long studyId) {
        Stream<AuditLog> auditlogEntries = auditLogRepository.listAuditlog(studyId);

        try {
            var generator = MapperUtils.MAPPER.getFactory().createGenerator(outputStream, JsonEncoding.UTF8);
            generator.writeStartArray();
            // stream rausposten
            auditlogEntries.forEach(auditlogEntry -> {
                try {
                    MapperUtils.MAPPER.writeValue(generator, auditlogEntry);
                } catch(IOException e) {
                    throw new UncheckedIOException("Error exporting auditlogs for study " + studyId, e);
                }
            });
            generator.writeEndArray();
            generator.close();
        } catch(IOException e) {
            throw new UncheckedIOException("Error exporting auditlogs for study " + studyId, e);
        }
    }

    public long countAuditlogEntries(long studyId) {
        return auditLogRepository.countAuditlogEntries(studyId);
    }

    public List<AuditLog> listAuditlog(long studyId) {
        try (Stream<AuditLog> result = auditLogRepository.listAuditlog(studyId)) {
            return result != null
                    ? result.toList()
                    : Collections.emptyList();
        }
    }

    /**
     * Records the parsed auditLog if the referenced study is in a state that requires audits. Otherwise
     * the parsed log in not recorded and an empty {@link Optional} is returned
     * @param auditLog the auditLog to record
     * @return the recorded AuditLog (with '<code>id</code>') or an empty Optional if the parsed log was not recoded
     */
    public Optional<AuditLog> record(AuditLog auditLog){
        if(auditLog == null){
            return null;
        } else if(auditLog.getId() != null){
            throw new IllegalArgumentException("Unable to record existing AuditLog. The 'id' of the parsed AuditLog MUST BE NULL!");
        }
        if(auditProperties.studyStates() == null ||
                auditProperties.studyStates().isEmpty() ||
                studystateService.hasStudyState(auditLog.getStudyId(), auditProperties.studyStates())){
            return Optional.of(auditLogRepository.insert(auditLog));
        } else {
            return Optional.empty();
        }
    }

}
