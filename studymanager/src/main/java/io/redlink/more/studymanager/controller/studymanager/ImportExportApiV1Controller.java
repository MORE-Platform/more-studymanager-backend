/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.api.v1.model.GenerateDownloadToken200ResponseDTO;
import io.redlink.more.studymanager.api.v1.model.StudyDTO;
import io.redlink.more.studymanager.api.v1.model.StudyImportExportDTO;
import io.redlink.more.studymanager.api.v1.webservices.ImportExportApi;
import io.redlink.more.studymanager.controller.RequiresStudyRole;
import io.redlink.more.studymanager.model.DownloadToken;
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.model.transformer.ImportExportTransformer;
import io.redlink.more.studymanager.model.transformer.StudyTransformer;
import io.redlink.more.studymanager.repository.DownloadTokenRepository;
import io.redlink.more.studymanager.service.ImportExportService;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import io.redlink.more.studymanager.utils.MapperUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping(value = "/api/v1")
public class ImportExportApiV1Controller implements ImportExportApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(StudyApiV1Controller.class);

    private final ImportExportService service;

    private final DownloadTokenRepository tokenRepository;

    private final OAuth2AuthenticationService authService;


    public ImportExportApiV1Controller(ImportExportService service, DownloadTokenRepository tokenRepository, OAuth2AuthenticationService authService) {
        this.service = service;
        this.tokenRepository = tokenRepository;
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
    public ResponseEntity<StreamingResponseBody> exportStudyData(Long studyId, String token, List<Integer> studyGroupId, List<Integer> participantId, List<Integer> observationId, Instant from, Instant to) {
        Optional<DownloadToken> dt = tokenRepository.getToken(token).filter(t -> t.getStudyId().equals(studyId));

        if (dt.isPresent()) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Content-Disposition", "attachment;filename=" + dt.get().getFilename());

            return ResponseEntity
                    .ok()
                    .headers(responseHeaders)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(outputStream -> {
                        try {
                            service.exportStudyData(outputStream, studyId, studyGroupId, participantId, observationId, from, to);
                        } catch (Exception e) {
                            LOGGER.warn("Error exporting study data for study_{}: {}", studyId, e.getMessage(), e);
                        }
                    });
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<GenerateDownloadToken200ResponseDTO> generateDownloadToken(Long studyId, List<Integer> studyGroupId, List<Integer> participantId, List<Integer> observationId, Instant from, Instant to) {
        var token = tokenRepository.createToken(studyId).getToken();
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().pathSegment(token).build(true).toUri();

        return ResponseEntity.created(uri).body(new GenerateDownloadToken200ResponseDTO().token(token));
    }

    @Override
    public ResponseEntity<StudyDTO> importStudy(MultipartFile file) {
        try {
            final var currentUser = authService.getCurrentUser();
            StudyImportExportDTO imp = MapperUtils.MAPPER.readValue(file.getInputStream(), StudyImportExportDTO.class);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    StudyTransformer.toStudyDTO_V1(
                            service.importStudy(ImportExportTransformer.fromStudyImportExportDTO_V1(imp), currentUser)
                    )
            );
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
