/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.component.observation.lime.LimeSurveyObservationFactory;
import io.redlink.more.studymanager.sdk.MoreSDK;
import io.redlink.more.studymanager.service.ObservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1")
public class LimeSurveySidecarController {

    private final LimeSurveyObservationFactory factory;

    private final ObservationService observationService;

    private final MoreSDK sdk;

    public LimeSurveySidecarController(LimeSurveyObservationFactory factory, ObservationService observationService, MoreSDK sdk) {
        this.factory = factory;
        this.observationService = observationService;
        this.sdk = sdk;
    }

    /**
     * This is a temporary solution that should actually run as a sidecard and use an client credentials flow to access the limesurvey component api
     * @return a html response
     */
    @RequestMapping(
            method = RequestMethod.GET,
            value = "/components/observation/lime-survey-observation/end.html",
            produces = { "text/html" }
    )
    public ResponseEntity<String> getLimeSurveyEndPageAndWriteResult(
            @RequestParam("studyId") Long studyId,
            @RequestParam("observationId") Integer observationId,
            @RequestParam("token") String token,
            @RequestParam("surveyid") Integer surveyid,
            @RequestParam("savedid") Integer savedid
    ) {
        return observationService.getObservation(studyId, observationId)
                .map(o -> factory.create(sdk.scopedObservationSDK(o.getStudyId(), o.getStudyGroupId(), o.getObservationId()), o.getProperties()))
                .map(obs -> obs.writeDataPoints(token, surveyid, savedid))
                .map(r -> r ? true : null)
                .map(r ->  ResponseEntity.ok("<h1>Survey submitted</h1>"))
                .orElse(ResponseEntity.status(401).build());
    }

}
