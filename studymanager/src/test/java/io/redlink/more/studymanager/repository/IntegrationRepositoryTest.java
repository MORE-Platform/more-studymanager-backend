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
        Long studyId = studyRepository.insert(new Study()
                        .setContact(new Contact().setPerson("test").setEmail("test")))
                .getStudyId();
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
                        .setRRule(new RecurrenceRule().setFreq("DAILY").setCount(7)))
                .setHidden(true);

        final Observation finalObservation = observationRepository.insert(observation);

        final String tokenSecret1 = "123";
        final String tokenSecret2 = "456";
        final String tokenLabel1 = "token1";
        final String tokenLabel2 = "token2";

        Optional<EndpointToken> integrationResponse1 = integrationRepository.addToken(studyId, finalObservation.getObservationId(), tokenLabel1, tokenSecret1);
        assertThat(integrationResponse1).isPresent();
        assertThat(integrationRepository.addToken(studyId, finalObservation.getObservationId(), tokenLabel1, tokenSecret1)).isEmpty();

        assertThat(integrationResponse1.get().tokenId()).isNotNull();
        assertThat(integrationResponse1.get().tokenLabel()).isEqualTo(tokenLabel1);
        assertThat(integrationResponse1.get().token()).isNull();
        assertThat(integrationResponse1.get().created()).isNotNull();

        Optional<EndpointToken> integrationResponse2 = integrationRepository.addToken(studyId, finalObservation.getObservationId(), tokenLabel2, tokenSecret2);
        assertThat(integrationResponse2).isPresent();
        assertThat(integrationResponse2.get().tokenId()).isNotEqualTo(integrationResponse1.get().tokenId());


        Optional<EndpointToken> integrationResponse3 = integrationRepository.getToken(studyId, finalObservation.getObservationId(), integrationResponse1.get().tokenId());
        assertThat(integrationResponse3).isPresent();

        assertThat(integrationResponse3.get().tokenId()).isNotNull();
        assertThat(integrationResponse3.get().tokenLabel()).isEqualTo(tokenLabel1);
        assertThat(integrationResponse3.get().token()).isNull();
        assertThat(integrationResponse3.get().created()).isNotNull();


        List<EndpointToken> integrationResponse4 = integrationRepository.getAllTokens(studyId, finalObservation.getObservationId());

        assertThat(integrationResponse4.size()).isEqualTo(2);
        assertThat(integrationResponse4.get(0).tokenId()).isNotNull();
        assertThat(integrationResponse4.get(0).tokenLabel()).isEqualTo(tokenLabel1);
        assertThat(integrationResponse4.get(0).token()).isNull();
        assertThat(integrationResponse4.get(0).created()).isNotNull();

        assertThat(integrationResponse4.get(1).tokenId()).isNotNull();
        assertThat(integrationResponse4.get(1).tokenLabel()).isEqualTo(tokenLabel2);
        assertThat(integrationResponse4.get(1).token()).isNull();
        assertThat(integrationResponse4.get(1).created()).isNotNull();

        integrationRepository.deleteToken(studyId, finalObservation.getObservationId(), integrationResponse2.get().tokenId());
        integrationRepository.deleteToken(studyId, finalObservation.getObservationId(), integrationResponse2.get().tokenId());
        assertThat(integrationRepository.getToken(studyId, finalObservation.getObservationId(), integrationResponse2.get().tokenId())).isEmpty();

        List<EndpointToken> integrationResponse5 = integrationRepository.getAllTokens(studyId , finalObservation.getObservationId());
        assertThat(integrationResponse5.size()).isEqualTo(1);
        assertThat(integrationResponse5.get(0).tokenId()).isNotNull();
        assertThat(integrationResponse5.get(0).tokenLabel()).isEqualTo(tokenLabel1);
        assertThat(integrationResponse5.get(0).token()).isNull();
        assertThat(integrationResponse5.get(0).created()).isNotNull();

        integrationRepository.deleteToken(studyId, finalObservation.getObservationId(), integrationResponse1.get().tokenId());
        assertThat(integrationRepository.getAllTokens(studyId, finalObservation.getObservationId()).size()).isEqualTo(0);
    }

    @Test
    @DisplayName("Testing token deletion, when corresponding observation or study gets deleted")
    public void testRepositoryCascade() {
        String type = "accelerometer";
        Long studyId1 = studyRepository.insert(new Study().setContact(new Contact().setPerson("test").setEmail("test")))
                .getStudyId();
        Integer studyGroupId = studyGroupRepository.insert(new StudyGroup().setStudyId(studyId1)).getStudyGroupId();
        Instant startTime = Instant.now();
        Instant endTime = Instant.now().plus(2, ChronoUnit.HOURS);

        Observation observation = new Observation()
                .setStudyId(studyId1)
                .setType(type)
                .setTitle("some title")
                .setStudyGroupId(studyGroupId)
                .setProperties(new ObservationProperties(Map.of("testProperty", "testValue")))
                .setSchedule(new Event()
                        .setDateStart(startTime)
                        .setDateEnd(endTime)
                        .setRRule(new RecurrenceRule().setFreq("DAILY").setCount(7)))
                .setHidden(true);

        final Observation finalObservation = observationRepository.insert(observation);

        final String tokenLabel = "token1";
        final String tokenSecret = "123";

        integrationRepository.addToken(studyId1, finalObservation.getObservationId(), tokenLabel, tokenSecret);
        assertThat(integrationRepository.getAllTokens(studyId1, finalObservation.getObservationId()).size()).isEqualTo(1);
        observationRepository.deleteObservation(studyId1, finalObservation.getObservationId());
        assertThat(integrationRepository.getAllTokens(studyId1, finalObservation.getObservationId()).size()).isEqualTo(0);

        final Observation finalObservation2 = observationRepository.insert(observation);

        integrationRepository.addToken(studyId1, finalObservation2.getObservationId(), tokenLabel, tokenSecret);
        assertThat(integrationRepository.getAllTokens(studyId1, finalObservation.getObservationId()).size()).isEqualTo(1);
        studyRepository.deleteById(studyId1);
        assertThat(integrationRepository.getAllTokens(studyId1, finalObservation.getObservationId()).size()).isEqualTo(0);

        Long studyId2 = studyRepository.insert(new Study().setContact(new Contact().setPerson("test").setEmail("test"))).getStudyId();
        observation.setStudyGroupId(studyGroupRepository.insert(new StudyGroup().setStudyId(studyId2)).getStudyGroupId());
        observation.setStudyId(studyId2);
        final Observation finalObservation3 = observationRepository.insert(observation);

        integrationRepository.addToken(studyId2, finalObservation3.getObservationId(), tokenLabel, tokenSecret);
        integrationRepository.clearForStudyId(studyId2);
        assertThat(integrationRepository.getAllTokens(studyId2, finalObservation3.getObservationId()).size()).isEqualTo(0);
    }
}
