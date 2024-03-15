/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.component.action;

import io.redlink.more.studymanager.core.component.Action;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.io.ActionParameter;
import io.redlink.more.studymanager.core.properties.ActionProperties;
import io.redlink.more.studymanager.core.sdk.MoreActionSDK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PushNotificationAction extends Action<ActionProperties> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PushNotificationAction.class);
    protected PushNotificationAction(MoreActionSDK sdk, ActionProperties properties) throws ConfigurationValidationException {
        super(sdk, properties);
    }

    @Override
    public void execute(ActionParameter parameters) {
        LOGGER.info("send push notification with parameters: {}", parameters.toString());
        sdk.sendPushNotification(
                properties.getString("title"),
                properties.getString("message")
        );
    }
}
