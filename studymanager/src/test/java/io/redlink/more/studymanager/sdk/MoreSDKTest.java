package io.redlink.more.studymanager.sdk;

import io.redlink.more.studymanager.core.factory.TriggerFactory;
import io.redlink.more.studymanager.core.io.Parameters;
import io.redlink.more.studymanager.core.io.TriggerResult;
import io.redlink.more.studymanager.core.sdk.schedule.CronSchedule;
import io.redlink.more.studymanager.model.Trigger;
import io.redlink.more.studymanager.service.ActionService;
import io.redlink.more.studymanager.service.InterventionService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.quartz.JobExecutionException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
}
