package io.redlink.more.studymanager.sdk;

import io.redlink.more.studymanager.core.sdk.schedule.CronSchedule;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

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

    @Test
    public void testTriggerScheduling() throws InterruptedException, JobExecutionException {
        String id = moreSDK.addSchedule("i1", 1,null, 1, new CronSchedule("* * * ? * *"));
        TimeUnit.SECONDS.sleep(1);
        ArgumentCaptor<JobExecutionContext> captor = ArgumentCaptor.forClass(JobExecutionContext.class);
        verify(moreSDK, atLeast(1)).testPing(captor.capture());
        assertThat(captor.getValue().getJobDetail().getJobDataMap().get("studyId")).isEqualTo(1L);
        moreSDK.removeSchedule("i1", id);
        reset(moreSDK);
        TimeUnit.SECONDS.sleep(1);
        verify(moreSDK, never()).testPing(any());

    }
}
