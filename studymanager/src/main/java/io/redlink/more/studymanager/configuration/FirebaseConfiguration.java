package io.redlink.more.studymanager.configuration;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import io.redlink.more.studymanager.properties.FirebaseProperties;
import io.redlink.more.studymanager.service.FirebaseMessagingService;
import java.io.FileNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(FirebaseProperties.class)
public class FirebaseConfiguration {
    private static final Logger log = LoggerFactory.getLogger(FirebaseConfiguration.class);
    private final FirebaseProperties props;

    public FirebaseConfiguration(FirebaseProperties props) {
        this.props = props;
    }

    @Bean
    public FirebaseMessagingService firebaseMessagingService() {
        return new FirebaseMessagingService(firebaseMessaging());
    }

    FirebaseMessaging firebaseMessaging() {
        try {
            final GoogleCredentials googleCredentials = GoogleCredentials
                    .fromStream(props.settingsFile().getInputStream());
            final FirebaseOptions firebaseOptions = FirebaseOptions
                    .builder()
                    .setCredentials(googleCredentials)
                    .build();
            final FirebaseApp app = FirebaseApp.initializeApp(firebaseOptions, "MORE Platform StudyManager");
            return FirebaseMessaging.getInstance(app);
        } catch (FileNotFoundException e) {
            log.warn("Could not load FirebaseOptions: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.warn("Could not create Firebase Messaging: {}", e.getMessage(), e);
            return null;
        }
    }
}
