package io.redlink.more.studymanager.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParticipantUtilsTest {
    @Test
    void testGetRegistrationUriWithEmptyToken() {
        // Test with null token
        assertThat(ParticipantUtils.getRegistrationUri(null))
                .as("Registration URI with null token should be empty")
                .isEmpty();

        // Test with empty token
        assertThat(ParticipantUtils.getRegistrationUri(""))
                .as("Registration URI with empty token should be empty")
                .isEmpty();
    }

    @Test
    void testGetRegistrationUriWithValidToken() throws UnknownHostException {
        String hostname = "test-host";
        String token = "test-token";
        String expectedUri = "https://" + hostname + "/api/v1/signup?token=" + token;

        try (MockedStatic<InetAddress> mockedInetAddress = Mockito.mockStatic(InetAddress.class)) {
            InetAddress mockedAddress = Mockito.mock(InetAddress.class);
            when(mockedAddress.getHostName()).thenReturn(hostname);
            mockedInetAddress.when(InetAddress::getLocalHost).thenReturn(mockedAddress);

            String actualUri = ParticipantUtils.getRegistrationUri(token);

            assertThat(actualUri)
                    .as("Registration URI should be correctly constructed with hostname and token")
                    .isEqualTo(expectedUri);
        }
    }

    @Test
    void testGetRegistrationUriWithHostnameException() {
        String token = "test-token";
        String expectedUri = "https://localhost/api/v1/signup?token=" + token;

        try (MockedStatic<InetAddress> mockedInetAddress = Mockito.mockStatic(InetAddress.class)) {
            mockedInetAddress.when(InetAddress::getLocalHost).thenThrow(new UnknownHostException("Test exception"));

            String actualUri = ParticipantUtils.getRegistrationUri(token);

            assertThat(actualUri)
                    .as("Registration URI should use 'localhost' when hostname cannot be determined")
                    .isEqualTo(expectedUri);
        }
    }
}
