package io.redlink.more.studymanager.configuration;

import io.redlink.more.studymanager.component.observation.LimeSurveyObservationFactory;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;

import java.util.Map;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(classes = ComponentFactoriesConfiguration.class)
public class ComponentConfigurationTest {

    @Autowired
    ApplicationContext context;
    @Test
    public void testConfig() {
        LimeSurveyObservationFactory factory = context.getBean(LimeSurveyObservationFactory.class);
        Assertions.assertEquals(factory.componentProperties.get("url"), "https://lime.platform-test.more.redlink.io/admin/remotecontrol");
    }
}
