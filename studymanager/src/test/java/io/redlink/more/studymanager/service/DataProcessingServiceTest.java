package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.ParticipationData;
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

    @InjectMocks
    DataProcessingService dataProcessingService;

    @ParameterizedTest
    @MethodSource("provideParticipationArguments")
    void completeParticipationDataTest(List<Participant> participants,
                                       List<Observation> observations,
                                       List<ParticipationData> participationsGiven,
                                       List<ParticipationData> participationsExpected){

        when(observationService.listObservations(anyLong())).thenReturn(observations);
        when(participantService.listParticipants(anyLong())).thenReturn(participants);

        assertThat(dataProcessingService.completeParticipationData(participationsGiven, 1L))
                .isEqualTo(participationsExpected);
    }

    private static Stream<Arguments> provideParticipationArguments(){
        Participant participant1 = new Participant()
                .setParticipantId(1)
                .setStudyId(1L)
                .setStudyGroupId(1);
        Participant participant2 = new Participant()
                .setParticipantId(2)
                .setStudyId(1L)
                .setStudyGroupId(1);
        Participant participant3 = new Participant()
                .setParticipantId(3)
                .setStudyId(1L)
                .setStudyGroupId(2);

        Observation observation1 = new Observation()
                .setObservationId(1)
                .setStudyGroupId(1);
        Observation observation2 = new Observation()
                .setObservationId(2);


        ParticipationData participation1 =
                new ParticipationData(1,
                        participant1.getParticipantId(),
                        participant1.getStudyGroupId(),
                        true,
                        Instant.parse("2023-02-06T10:03:00.00Z"));
        ParticipationData participation2 =
                new ParticipationData(1,
                        participant2.getParticipantId(),
                        participant2.getStudyGroupId(),
                        true,
                        Instant.parse("2022-02-06T10:03:00.00Z"));
        ParticipationData participation3 =
                new ParticipationData(2,
                        participant1.getParticipantId(),
                        participant1.getStudyGroupId(),
                        true,
                        Instant.parse("2021-02-06T10:03:00.00Z"));
        ParticipationData participation4 =
                new ParticipationData(2,
                        participant2.getParticipantId(),
                        participant2.getStudyGroupId(),
                        true,
                        Instant.parse("2020-02-06T10:03:00.00Z"));
        ParticipationData participation5 =
                new ParticipationData(2,
                        participant3.getParticipantId(),
                        participant3.getStudyGroupId(),
                        true,
                        Instant.parse("2019-02-06T10:03:00.00Z"));

        List<Observation> observations = List.of(observation1, observation2);
        List<Participant> participants = List.of(participant1, participant2, participant3);

        List<ParticipationData> participationsGiven1 = new ArrayList<>();
        List<ParticipationData> participationsGiven2 = new ArrayList<>(List.of(participation1, participation2, participation3, participation4, participation5));
        List<ParticipationData> participationsGiven3 = new ArrayList<>(List.of(participation1, participation3, participation5));

        List<ParticipationData> participationsExpected1 = List.of(
                new ParticipationData(1,1,1,false,null),
                new ParticipationData(1,2,1,false,null),
                new ParticipationData(2,1,1,false,null),
                new ParticipationData(2,2,1,false,null),
                new ParticipationData(2,3,2,false,null)
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
                new ParticipationData(1,2,1,false,null),
                participation3,
                new ParticipationData(2,2,1,false,null),
                participation5
        );

        return Stream.of(
                Arguments.of(participants, observations, participationsGiven1, participationsExpected1),
                Arguments.of(participants, observations, participationsGiven2, participationsExpected2),
                Arguments.of(participants, observations, participationsGiven3, participationsExpected3)
        );
    }

}
