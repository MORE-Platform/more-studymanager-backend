package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.api.v1.model.StudyImportExportDTO;
import io.redlink.more.studymanager.api.v1.webservices.ImportExportApi;
import io.redlink.more.studymanager.controller.RequiresStudyRole;
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.model.transformer.ImportExportTransformer;
import io.redlink.more.studymanager.service.ImportExportService;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
@RequestMapping(value = "/api/v1")
public class ImportExportApiV1Controller implements ImportExportApi {

    private final ImportExportService service;

    private final OAuth2AuthenticationService authService;


    public ImportExportApiV1Controller(ImportExportService service, OAuth2AuthenticationService authService) {
        this.service = service;
        this.authService = authService;
    }


    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<Resource> exportParticipants(Long studyId) {
        final var currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(
                service.exportParticipants(studyId, currentUser)
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<Void> importParticipants(Long studyId, MultipartFile file) {
        try {
            service.importParticipants(studyId, file.getInputStream());
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(201).build();
    }


    @Override
    @RequiresStudyRole(StudyRole.STUDY_ADMIN)
    public ResponseEntity<StudyImportExportDTO> exportStudy(Long studyId) {
        final var currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(
                ImportExportTransformer
                        .toStudyImportExportDTO_V1(
                                service.exportStudy(studyId, currentUser)
                        )
        );
    }
    @Override
    public ResponseEntity<Void> importStudy(Long studyId, StudyImportExportDTO studyImportExportDTO) {
        service.importStudy(
                studyId,
                ImportExportTransformer
                        .fromStudyImportExportDTO_V1(studyImportExportDTO)
        );
        return ResponseEntity.status(201).build();
    }
}
