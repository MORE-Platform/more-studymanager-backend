package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test-containers-flyway")
public class IntegrationRepositoryTest {

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private ObservationRepository observationRepository;

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @Autowired
    private IntegrationRepository integrationRepository;

    @BeforeEach
    void deleteAll() { integrationRepository.clear(); }

    @Test
    @DisplayName("Testing all operations on IntegrationRepository")
    public void testAddGetListDelete() {
        String type = "accelerometer";
        Long studyId = studyRepository.insert(new Study()).getStudyId();
        Integer studyGroupId = studyGroupRepository.insert(new StudyGroup().setStudyId(studyId)).getStudyGroupId();
        Instant startTime = Instant.now();
        Instant endTime = Instant.now().plus(2, ChronoUnit.HOURS);

        Observation observation = new Observation()
                .setStudyId(studyId)
                .setType(type)
                .setTitle("some title")
                .setStudyGroupId(studyGroupId)
                .setProperties(new ObservationProperties(Map.of("testProperty", "testValue")))
                .setSchedule(new Event()
                        .setDateStart(startTime)
                        .setDateEnd(endTime)
                        .setRRule(new RecurrenceRule().setFreq("DAILY").setCount(7)));

        final Observation finalObservation = observationRepository.insert(observation);

        EndpointToken endpointToken1 = new EndpointToken(
                "token1",
                "123"
        );

        EndpointToken endpointToken2 = new EndpointToken(
                "token2",
                "456"
        );

        Optional<EndpointToken> integrationResponse1 = integrationRepository.addToken(studyId, finalObservation.getObservationId(), endpointToken1);
        assertThat(integrationResponse1).isPresent();
        assertThat(integrationRepository.addToken(studyId, finalObservation.getObservationId(), endpointToken1)).isEmpty();

        assertThat(integrationResponse1.get().tokenId()).isNotNull();
        assertThat(integrationResponse1.get().tokenLabel()).isEqualTo(endpointToken1.tokenLabel());
        assertThat(integrationResponse1.get().token()).isEqualTo(endpointToken1.token());
        assertThat(integrationResponse1.get().created()).isNotNull();

        Optional<EndpointToken> integrationResponse2 = integrationRepository.addToken(studyId, finalObservation.getObservationId(), endpointToken2);
        assertThat(integrationResponse2).isPresent();
        assertThat(integrationResponse2.get().tokenId()).isNotEqualTo(integrationResponse1.get().tokenId());


        Optional<EndpointToken> integrationResponse3 = integrationRepository.getToken(studyId, finalObservation.getObservationId(), integrationResponse1.get().tokenId());
        assertThat(integrationResponse3).isPresent();

        assertThat(integrationResponse3.get().tokenId()).isNotNull();
        assertThat(integrationResponse3.get().tokenLabel()).isEqualTo(endpointToken1.tokenLabel());
        assertThat(integrationResponse3.get().token()).isEqualTo(endpointToken1.token());
        assertThat(integrationResponse3.get().created()).isNotNull();


        List<EndpointToken> integrationResponse4 = integrationRepository.getAllTokens(studyId, finalObservation.getObservationId());

        assertThat(integrationResponse4.size()).isEqualTo(2);
        assertThat(integrationResponse4.get(0).tokenId()).isNotNull();
        assertThat(integrationResponse4.get(0).tokenLabel()).isEqualTo(endpointToken1.tokenLabel());
        assertThat(integrationResponse4.get(0).token()).isEqualTo(endpointToken1.token());
        assertThat(integrationResponse4.get(0).created()).isNotNull();

        assertThat(integrationResponse4.get(1).token()).isNotNull();
        assertThat(integrationResponse4.get(1).tokenLabel()).isEqualTo(endpointToken2.tokenLabel());
        assertThat(integrationResponse4.get(1).token()).isEqualTo(endpointToken2.token());
        assertThat(integrationResponse4.get(1).created()).isNotNull();

        integrationRepository.deleteToken(studyId, finalObservation.getObservationId(), integrationResponse2.get().tokenId());
        integrationRepository.deleteToken(studyId, finalObservation.getObservationId(), integrationResponse2.get().tokenId());
        assertThat(integrationRepository.getToken(studyId, finalObservation.getObservationId(), integrationResponse2.get().tokenId())).isEmpty();

        List<EndpointToken> integrationResponse5 = integrationRepository.getAllTokens(studyId , finalObservation.getObservationId());
        assertThat(integrationResponse5.size()).isEqualTo(1);
        assertThat(integrationResponse5.get(0).tokenId()).isNotNull();
        assertThat(integrationResponse5.get(0).tokenLabel()).isEqualTo(endpointToken1.tokenLabel());
        assertThat(integrationResponse5.get(0).token()).isEqualTo(endpointToken1.token());
        assertThat(integrationResponse5.get(0).created()).isNotNull();

        integrationRepository.deleteToken(studyId, finalObservation.getObservationId(), integrationResponse1.get().tokenId());
        assertThat(integrationRepository.getAllTokens(studyId, finalObservation.getObservationId()).size()).isEqualTo(0);
    }
}
