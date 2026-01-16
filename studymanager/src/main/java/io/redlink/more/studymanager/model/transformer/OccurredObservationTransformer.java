package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.OccurredObservationDTO;
import io.redlink.more.studymanager.controller.studymanager.OccurredObservationsApiV1Controller;
import io.redlink.more.studymanager.core.datavalidity.ObservationDataState;
import io.redlink.more.studymanager.properties.GatewayProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OccurredObservationTransformer {
    private static final Logger log = LoggerFactory.getLogger(OccurredObservationTransformer.class);

    public static OccurredObservationDTO toOccurredObservationDTO_V1(OccurredObservationsApiV1Controller.OccurredObservationData ood, GatewayProperties gatewayProperties) {
        return new OccurredObservationDTO()
                .studyId(ood.occurredObservation().studyId())
                .observation(ObservationTransformer.toObservationDTO_V1(ood.observation()))
                .participant(ParticipantTransformer.toParticipantDTO_V1(ood.participant(), gatewayProperties))
                .start(ood.occurredObservation().start())
                .end(ood.occurredObservation().end())
                .state(!ood.occurredObservation().dataValid() ?
                        OccurredObservationDTO.StateEnum.MISSING :
                        toStateEnum(ood.occurredObservation().dataState()));
    }


    private static OccurredObservationDTO.StateEnum toStateEnum(ObservationDataState state) {
        try {
            return OccurredObservationDTO.StateEnum.valueOf(state.name());
        } catch (IllegalArgumentException e) {
            log.error("Unable to convert ObservationDataState {} to OccurredObservationDTO.StateEnum! Use MISSING as fallback.", state.name(), e);
            return OccurredObservationDTO.StateEnum.MISSING;
        }
    }
}