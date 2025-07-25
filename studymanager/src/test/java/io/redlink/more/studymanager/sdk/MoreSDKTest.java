/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.sdk;

import io.redlink.more.studymanager.action.ActionService;
import io.redlink.more.studymanager.core.factory.TriggerFactory;
import io.redlink.more.studymanager.core.io.Parameters;
import io.redlink.more.studymanager.core.io.Timeframe;
import io.redlink.more.studymanager.core.io.TriggerResult;
import io.redlink.more.studymanager.core.sdk.schedule.CronSchedule;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.Trigger;
import io.redlink.more.studymanager.service.ElasticService;
import io.redlink.more.studymanager.service.InterventionService;
import io.redlink.more.studymanager.service.ParticipantService;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test-containers-flyway")
class MoreSDKTest {
    @MockitoSpyBean
    MoreSDK moreSDK;

    @MockitoBean(name = "test-trigger")
    TriggerFactory triggerFactory;

    @MockitoBean
    InterventionService interventionService;

    @MockitoBean
    ActionService actionService;

    @MockitoBean
    ParticipantService participantService;

    @MockitoBean
    ElasticService elasticService;

    @Test
    void testTriggerScheduling() throws InterruptedException {
        Trigger triggerModel = spy(Trigger.class);
        io.redlink.more.studymanager.core.component.Trigger trigger =
                mock( io.redlink.more.studymanager.core.component.Trigger.class);
        TriggerResult triggerResult = mock(TriggerResult.class);

        when(triggerModel.getType()).thenReturn("test-trigger");
        when(interventionService.getTriggerByIds(any(),any())).thenReturn(triggerModel);
        when(triggerFactory.getId()).thenReturn("test-trigger");
        when(triggerFactory.create(any(), any())).thenReturn(trigger);
        when(trigger.execute(any())).thenReturn(triggerResult);
        when(triggerResult.proceed()).thenReturn(false);

        String id = moreSDK.addSchedule("i1", 1,null, 1, new CronSchedule("* * * ? * *"));
        TimeUnit.SECONDS.sleep(1);

        ArgumentCaptor<Long> studyIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Integer> studyGroupIdCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> interventionIdCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(moreSDK, atLeast(1))
                .scopedTriggerSDK(studyIdCaptor.capture(), studyGroupIdCaptor.capture(), interventionIdCaptor.capture());

        assertThat(studyIdCaptor.getValue()).isEqualTo(1L);
        assertThat(studyGroupIdCaptor.getValue()).isNull();
        assertThat(interventionIdCaptor.getValue()).isEqualTo(1);

        ArgumentCaptor<Parameters> parametersCaptor = ArgumentCaptor.forClass(Parameters.class);
        verify(trigger, atLeast(1)).execute(parametersCaptor.capture());

        assertThat(parametersCaptor.getValue()).containsKey("triggerTime");

        moreSDK.removeSchedule("i1", id);
        reset(trigger);

        TimeUnit.SECONDS.sleep(1);
        verify(trigger, never()).execute(any());

    }

    @Test
    void testListParticipants() {
        when(participantService.listParticipants(any())).thenReturn(List.of(
                new Participant().setParticipantId(1).setStatus(Participant.Status.ACTIVE),
                new Participant().setParticipantId(2).setStatus(Participant.Status.ACTIVE).setStudyGroupId(1),
                new Participant().setParticipantId(3).setStatus(Participant.Status.ACTIVE).setStudyGroupId(2),
                new Participant().setParticipantId(4).setStatus(Participant.Status.ACTIVE),
                new Participant().setParticipantId(5).setStatus(Participant.Status.NEW).setStudyGroupId(1),
                new Participant().setParticipantId(6).setStatus(Participant.Status.NEW)
        ));

        assertThat(moreSDK.listParticipants(1L, null, Set.of(Participant.Status.ACTIVE))).hasSize(4);
        assertThat(moreSDK.listParticipants(1L, 1, Set.of(Participant.Status.ACTIVE))).hasSize(1);
        assertThat(moreSDK.listParticipants(1L, 2, Set.of(Participant.Status.ACTIVE))).hasSize(1);

        when(elasticService.participantsThatMapQuery(any(), any(), any(), any())).thenReturn(
               List.of(1,5)
        );

        assertThat(
                moreSDK.listActiveParticipantsByQuery(
                        1L, null, "*", mock(Timeframe.class))
        ).containsExactlyInAnyOrder(1);
    }
}
