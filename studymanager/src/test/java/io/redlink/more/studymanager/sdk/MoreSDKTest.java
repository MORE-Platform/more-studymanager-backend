package io.redlink.more.studymanager.sdk;

import io.redlink.more.studymanager.core.factory.TriggerFactory;
import io.redlink.more.studymanager.core.io.Parameters;
import io.redlink.more.studymanager.core.io.Timeframe;
import io.redlink.more.studymanager.core.io.TriggerResult;
import io.redlink.more.studymanager.core.sdk.schedule.CronSchedule;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.Trigger;
import io.redlink.more.studymanager.action.ActionService;
import io.redlink.more.studymanager.service.ElasticService;
import io.redlink.more.studymanager.service.InterventionService;
import io.redlink.more.studymanager.service.ParticipantService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.quartz.JobExecutionException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test-containers-flyway")
public class MoreSDKTest {
    @SpyBean
    MoreSDK moreSDK;

    @MockBean(name = "test-trigger")
    TriggerFactory triggerFactory;

    @MockBean
    InterventionService interventionService;

    @MockBean
    ActionService actionService;

    @MockBean
    ParticipantService participantService;

    @MockBean
    ElasticService elasticService;

    @Test
    public void testTriggerScheduling() throws InterruptedException, JobExecutionException {
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

        assertThat(parametersCaptor.getValue().containsKey("triggerTime")).isTrue();

        moreSDK.removeSchedule("i1", id);
        reset(trigger);

        TimeUnit.SECONDS.sleep(1);
        verify(trigger, never()).execute(any());

    }

    @Test
    public void testListParticipants() {
        when(participantService.listParticipants(any())).thenReturn(List.of(
                new Participant().setParticipantId(1).setStatus(Participant.Status.ACTIVE),
                new Participant().setParticipantId(2).setStatus(Participant.Status.ACTIVE).setStudyGroupId(1),
                new Participant().setParticipantId(3).setStatus(Participant.Status.ACTIVE).setStudyGroupId(2),
                new Participant().setParticipantId(4).setStatus(Participant.Status.ACTIVE),
                new Participant().setParticipantId(5).setStatus(Participant.Status.NEW).setStudyGroupId(1),
                new Participant().setParticipantId(6).setStatus(Participant.Status.NEW)
        ));

        assertThat(moreSDK.listActiveParticipants(1L, null)).hasSize(4);
        assertThat(moreSDK.listActiveParticipants(1L, 1)).hasSize(1);
        assertThat(moreSDK.listActiveParticipants(1L, 2)).hasSize(1);

        when(elasticService.participantsThatMapQuery(any(), any(), any(), any())).thenReturn(
               List.of(1,5)
        );

        assertThat(
                moreSDK.listActiveParticipantsByQuery(
                        1L, null, "*", mock(Timeframe.class), false)
        ).containsExactlyInAnyOrder(1);
        assertThat(
                moreSDK.listActiveParticipantsByQuery(
                        1L, null, "*", mock(Timeframe.class), true)
        ).containsExactlyInAnyOrder(2,3,4);
    }
}
