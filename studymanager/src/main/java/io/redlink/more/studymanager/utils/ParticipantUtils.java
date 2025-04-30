package io.redlink.more.studymanager.utils;

import io.micrometer.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class ParticipantUtils {
    private static Environment environment = null;
    private static final String SIGNUP_PATH = "/signup";
    private static final String TOKEN_QUERY_NAME = "token";
    private static final Logger LOGGER = LoggerFactory.getLogger(ParticipantUtils.class);

    public ParticipantUtils(Environment environment) {
        ParticipantUtils.environment = environment;
    }

    public static String getRegistrationUri(String token) {
        if (StringUtils.isEmpty(token)) {
            return "";
        }

        String host;

        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            LOGGER.error("Host name not found, using 'localhost' instead");
            host = "localhost";
        }

        String apiBasePath = ParticipantUtils.environment.getProperty("spring.application.api-base-path");
        return "https://" +
                host +
                "/" +
                apiBasePath +
                SIGNUP_PATH +
                "?" +
                TOKEN_QUERY_NAME +
                "=" +
                token;
    }
}
