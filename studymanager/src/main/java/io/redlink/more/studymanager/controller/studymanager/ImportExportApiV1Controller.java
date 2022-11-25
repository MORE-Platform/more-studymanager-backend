package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.api.v1.webservices.ImportExportApi;
import io.redlink.more.studymanager.service.ImportExportService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/api/v1")
public class ImportExportApiV1Controller implements ImportExportApi {

    private final ImportExportService service;

    public ImportExportApiV1Controller(ImportExportService service) {
        this.service = service;
    }


    @Override
    public ResponseEntity<Resource> exportParticipants(Long studyId) {
        return ResponseEntity.ok(
                service.exportParticipants(studyId)
        );
    }
}
