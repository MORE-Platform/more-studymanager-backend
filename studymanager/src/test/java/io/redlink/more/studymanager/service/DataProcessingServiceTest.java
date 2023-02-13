package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.ParticipationData;
import io.redlink.more.studymanager.model.StudyGroup;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
public class DataProcessingServiceTest {

    @Mock
    ObservationService observationService;

    @Mock
    ParticipantService participantService;

    @Mock
    StudyGroupService studyGroupService;

    @Mock
    ElasticService elasticService;

    @InjectMocks
    DataProcessingService dataProcessingService;

    @ParameterizedTest
    @MethodSource("provideParticipationArguments")
    void completeParticipationDataTest(List<Participant> participants,
                                       List<Observation> observations,
                                       List<StudyGroup> studyGroups,
                                       List<ParticipationData> participationsGiven,
                                       List<ParticipationData> participationsExpected){

        when(observationService.listObservations(anyLong())).thenReturn(observations);
        when(participantService.listParticipants(anyLong())).thenReturn(participants);
        when(studyGroupService.listStudyGroups(anyLong())).thenReturn(studyGroups);
        when(elasticService.getParticipationData(anyLong())).thenReturn(participationsGiven);

        assertThat(dataProcessingService.getParticipationData(1L))
                .isEqualTo(participationsExpected);
    }

    private static Stream<Arguments> provideParticipationArguments(){
        Participant participant1 = new Participant()
                .setParticipantId(1)
                .setAlias("1")
                .setStudyId(1L)
                .setStudyGroupId(1);
        Participant participant2 = new Participant()
                .setParticipantId(2)
                .setAlias("2")
                .setStudyId(1L)
                .setStudyGroupId(1);
        Participant participant3 = new Participant()
                .setParticipantId(3)
                .setAlias("3")
                .setStudyId(1L)
                .setStudyGroupId(2);

        Observation observation1 = new Observation()
                .setObservationId(1)
                .setTitle("1")
                .setStudyGroupId(1)
                .setType("type1");
        Observation observation2 = new Observation()
                .setObservationId(2)
                .setTitle("2")
                .setType("type1");

        StudyGroup studyGroup1 = new StudyGroup()
                .setStudyGroupId(1)
                .setTitle("1");
        StudyGroup studyGroup2 = new StudyGroup()
                .setStudyGroupId(2)
                .setTitle("2");


        ParticipationData participation1 =
                new ParticipationData(
                        new ParticipationData.NamedId(observation1.getObservationId(), observation1.getTitle()),
                        "type1",
                        new ParticipationData.NamedId(participant1.getParticipantId(), participant1.getAlias()),
                        new ParticipationData.NamedId(participant1.getStudyGroupId(), participant1.getStudyGroupId().toString()),
                        true,
                        Instant.parse("2023-02-06T10:03:00.00Z"));
        ParticipationData participation2 =
                new ParticipationData(
                        new ParticipationData.NamedId(observation1.getObservationId(), observation1.getTitle()),
                        "type1",
                        new ParticipationData.NamedId(participant2.getParticipantId(), participant2.getAlias()),
                        new ParticipationData.NamedId(participant2.getStudyGroupId(), participant2.getStudyGroupId().toString()),
                        true,
                        Instant.parse("2022-02-06T10:03:00.00Z"));
        ParticipationData participation3 =
                new ParticipationData(
                        new ParticipationData.NamedId(observation2.getObservationId(), observation2.getTitle()),
                        "type1",
                        new ParticipationData.NamedId(participant1.getParticipantId(), participant1.getAlias()),
                        new ParticipationData.NamedId(participant1.getStudyGroupId(), participant1.getStudyGroupId().toString()),
                        true,
                        Instant.parse("2021-02-06T10:03:00.00Z"));
        ParticipationData participation4 =
                new ParticipationData(
                        new ParticipationData.NamedId(observation2.getObservationId(), observation2.getTitle()),
                        "type1",
                        new ParticipationData.NamedId(participant2.getParticipantId(), participant2.getAlias()),
                        new ParticipationData.NamedId(participant2.getStudyGroupId(), participant2.getStudyGroupId().toString()),
                        true,
                        Instant.parse("2020-02-06T10:03:00.00Z"));
        ParticipationData participation5 =
                new ParticipationData(
                        new ParticipationData.NamedId(observation2.getObservationId(), observation2.getTitle()),
                        "type1",
                        new ParticipationData.NamedId(participant3.getParticipantId(), participant3.getAlias()),
                        new ParticipationData.NamedId(participant3.getStudyGroupId(), participant3.getStudyGroupId().toString()),
                        true,
                        Instant.parse("2019-02-06T10:03:00.00Z"));

        ParticipationData missingParticipation1 = new ParticipationData(
                new ParticipationData.NamedId(1,"1"),
                "type1",
                new ParticipationData.NamedId(1,"1"),
                new ParticipationData.NamedId(1,"1"),
                false,null);
        ParticipationData missingParticipation2 = new ParticipationData(
                new ParticipationData.NamedId(1,"1"),
                "type1",
                new ParticipationData.NamedId(2,"2"),
                new ParticipationData.NamedId(1,"1"),
                false,null);
        ParticipationData missingParticipation3 = new ParticipationData(
                new ParticipationData.NamedId(2,"2"),
                "type1",
                new ParticipationData.NamedId(1,"1"),
                new ParticipationData.NamedId(1,"1"),
                false,null);
        ParticipationData missingParticipation4 = new ParticipationData(
                new ParticipationData.NamedId(2,"2"),
                "type1",
                new ParticipationData.NamedId(2,"2"),
                new ParticipationData.NamedId(1,"1"),
                false,null);
        ParticipationData missingParticipation5 = new ParticipationData(
                new ParticipationData.NamedId(2,"2"),
                "type1",
                new ParticipationData.NamedId(3,"3"),
                new ParticipationData.NamedId(2,"2"),
                false,null);

        List<Observation> observations = List.of(observation1, observation2);
        List<Participant> participants = List.of(participant1, participant2, participant3);
        List<StudyGroup> studyGroups = List.of(studyGroup1, studyGroup2);

        List<ParticipationData> participationsGiven1 = new ArrayList<>();
        List<ParticipationData> participationsGiven2 = new ArrayList<>(List.of(participation1, participation2, participation3, participation4, participation5));
        List<ParticipationData> participationsGiven3 = new ArrayList<>(List.of(participation1, participation3, participation5));

        List<ParticipationData> participationsExpected1 = List.of(
                missingParticipation1,
                missingParticipation2,
                missingParticipation3,
                missingParticipation4,
                missingParticipation5
        );
        List<ParticipationData> participationsExpected2 = List.of(
                participation1,
                participation2,
                participation3,
                participation4,
                participation5
        );
        List<ParticipationData> participationsExpected3 = List.of(
                participation1,
                missingParticipation2,
                participation3,
                missingParticipation4,
                participation5
        );
        return Stream.of(
                Arguments.of(participants, observations, studyGroups, participationsGiven1, participationsExpected1),
                Arguments.of(participants, observations, studyGroups, participationsGiven2, participationsExpected2),
                Arguments.of(participants, observations, studyGroups, participationsGiven3, participationsExpected3)
        );
    }

}
