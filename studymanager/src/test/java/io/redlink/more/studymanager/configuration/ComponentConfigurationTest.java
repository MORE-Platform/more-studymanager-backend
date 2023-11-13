/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.configuration;

import io.redlink.more.studymanager.component.observation.lime.LimeSurveyObservationFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        args = {
        "--more.components.lime-survey-observation.url=https://testurl",
                "--more.components.lime-survey-observation.username=testUsername",
                "--more.components.lime-survey-observation.password=testPassword"})
@ContextConfiguration(classes = ComponentFactoriesConfiguration.class)
public class ComponentConfigurationTest {

    @Autowired
    ApplicationContext context;

    @Test
    public void testConfig() {
        LimeSurveyObservationFactory factory = context.getBean(LimeSurveyObservationFactory.class);
        Assertions.assertEquals(factory.componentProperties.get("username"), "testUsername");
        Assertions.assertEquals(factory.componentProperties.get("password"), "testPassword");
        Assertions.assertEquals(factory.componentProperties.get("url"), "https://testurl");
    }
}
