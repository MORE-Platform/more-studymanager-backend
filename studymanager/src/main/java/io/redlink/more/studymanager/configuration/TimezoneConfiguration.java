package io.redlink.more.studymanager.configuration;

import io.redlink.more.studymanager.properties.TimezoneProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Time;
import java.time.ZoneId;
import java.util.TimeZone;

@Configuration
@EnableConfigurationProperties({TimezoneProperties.class})
public class TimezoneConfiguration {
    final TimezoneProperties properties;

    public TimezoneConfiguration(TimezoneProperties properties) {
        this.properties = properties;
    }

    @Bean
    public ZoneId ZoneId() {
        return TimeZone().toZoneId();
    }

    @Bean public TimeZone TimeZone() { return TimeZone.getTimeZone(properties.identifier()); }
}
