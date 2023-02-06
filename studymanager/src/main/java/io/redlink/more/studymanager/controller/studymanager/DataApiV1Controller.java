package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.api.v1.model.ParticipationDataDTO;
import io.redlink.more.studymanager.api.v1.webservices.DataApi;
import io.redlink.more.studymanager.controller.RequiresStudyRole;
import io.redlink.more.studymanager.model.transformer.StudyDataTransformer;
import io.redlink.more.studymanager.service.ElasticService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class DataApiV1Controller implements DataApi {

    private final ElasticService elasticService;

    public DataApiV1Controller(ElasticService elasticService){
        this.elasticService = elasticService;
    }

    @Override
    @RequiresStudyRole
    public ResponseEntity<List<ParticipationDataDTO>> getParticipationData(Long studyId){
        return ResponseEntity.ok().body(
                elasticService.getParticipationData(studyId).stream()
                        .map(StudyDataTransformer::toParticipationDataDTO_V1)
                        .toList()
        );
    }
}