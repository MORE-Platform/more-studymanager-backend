package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.exception.BadRequestException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

        EndpointToken endpointToken1 = new EndpointToken()
                .setTokenLabel("token1")
                .setToken("123");

        EndpointToken endpointToken2 = new EndpointToken()
                .setTokenLabel("token2")
                .setToken("456");

        EndpointToken integrationResponse1 = integrationRepository.addToken(studyId, finalObservation.getObservationId(), endpointToken1);
        assertThrows(BadRequestException.class, () -> integrationRepository.addToken(studyId, finalObservation.getObservationId(), endpointToken1));

        assertThat(integrationResponse1.getTokenId()).isNotNull();
        assertThat(integrationResponse1.getTokenLabel()).isEqualTo(endpointToken1.getTokenLabel());
        assertThat(integrationResponse1.getToken()).isEqualTo(endpointToken1.getToken());
        assertThat(integrationResponse1.getCreated()).isNotNull();

        EndpointToken integrationResponse2 = integrationRepository.addToken(studyId, finalObservation.getObservationId(), endpointToken2);
        assertThat(integrationResponse2.getTokenId()).isNotEqualTo(integrationResponse1.getTokenId());


        EndpointToken integrationResponse3 = integrationRepository.getToken(studyId, finalObservation.getObservationId(), integrationResponse1.getTokenId());

        assertThat(integrationResponse3.getTokenId()).isNotNull();
        assertThat(integrationResponse3.getTokenLabel()).isEqualTo(endpointToken1.getTokenLabel());
        assertThat(integrationResponse3.getToken()).isEqualTo(endpointToken1.getToken());
        assertThat(integrationResponse3.getCreated()).isNotNull();


        List<EndpointToken> integrationResponse4 = integrationRepository.getAllTokens(studyId, finalObservation.getObservationId());

        assertThat(integrationResponse4.size()).isEqualTo(2);
        assertThat(integrationResponse4.get(0).getTokenId()).isNotNull();
        assertThat(integrationResponse4.get(0).getTokenLabel()).isEqualTo(endpointToken1.getTokenLabel());
        assertThat(integrationResponse4.get(0).getToken()).isEqualTo(endpointToken1.getToken());
        assertThat(integrationResponse4.get(0).getCreated()).isNotNull();

        assertThat(integrationResponse4.get(1).getTokenId()).isNotNull();
        assertThat(integrationResponse4.get(1).getTokenLabel()).isEqualTo(endpointToken2.getTokenLabel());
        assertThat(integrationResponse4.get(1).getToken()).isEqualTo(endpointToken2.getToken());
        assertThat(integrationResponse4.get(1).getCreated()).isNotNull();

        integrationRepository.deleteToken(studyId, finalObservation.getObservationId(), integrationResponse2.getTokenId());
        integrationRepository.deleteToken(studyId, finalObservation.getObservationId(), integrationResponse2.getTokenId());
        assertThrows(BadRequestException.class, () -> integrationRepository.getToken(studyId, finalObservation.getObservationId(), integrationResponse2.getTokenId()));

        List<EndpointToken> integrationResponse5 = integrationRepository.getAllTokens(studyId , finalObservation.getObservationId());
        assertThat(integrationResponse5.size()).isEqualTo(1);
        assertThat(integrationResponse5.get(0).getTokenId()).isNotNull();
        assertThat(integrationResponse5.get(0).getTokenLabel()).isEqualTo(endpointToken1.getTokenLabel());
        assertThat(integrationResponse5.get(0).getToken()).isEqualTo(endpointToken1.getToken());
        assertThat(integrationResponse5.get(0).getCreated()).isNotNull();

        integrationRepository.deleteToken(studyId, finalObservation.getObservationId(), integrationResponse1.getTokenId());
        assertThat(integrationRepository.getAllTokens(studyId, finalObservation.getObservationId()).size()).isEqualTo(0);
    }
}
