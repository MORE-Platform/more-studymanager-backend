package io.redlink.more.studymanager.audit;

import io.redlink.more.studymanager.api.v1.model.StudyDTO;
import io.redlink.more.studymanager.model.AuthenticatedUser;
import io.redlink.more.studymanager.model.PlatformRole;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.audit.AuditLog;
import io.redlink.more.studymanager.properties.AuditProperties;
import io.redlink.more.studymanager.service.AuditService;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.CodeSignature;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.*;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class AuditAspectTest {

    AuditService auditService = Mockito.mock(AuditService.class);
    OAuth2AuthenticationService authService = Mockito.mock(OAuth2AuthenticationService.class);
    AuthenticatedUser authUser = Mockito.mock(AuthenticatedUser.class);

    AuditAspect auditAspect = new AuditAspect(
            new AuditProperties(EnumSet.of(Study.Status.ACTIVE, Study.Status.PAUSED, Study.Status.CLOSED), -1L),
            auditService,
            authService);

    @Before
    public void setup() {
        when(authUser.id()).thenReturn("test-user");
        when(authUser.roles()).thenReturn(Set.of(PlatformRole.MORE_VIEWER));
        when(authService.getCurrentUser()).thenReturn(authUser);
        when(auditService.record(any())).thenAnswer(invocationOnMock -> Optional.of(invocationOnMock.getArguments()[0]));
    }

    @Test
    public void testRecordStudyActivity() throws Throwable {

        // Creating mock objects
        JoinPoint joinPoint = Mockito.mock(JoinPoint.class);
        CodeSignature signature = Mockito.mock(CodeSignature.class);
        ResponseEntity<Void> responseEntity = Mockito.mock(ResponseEntity.class);
        HttpHeaders httpHeaders = Mockito.mock(HttpHeaders.class);

        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(responseEntity.getHeaders()).thenReturn(httpHeaders);
        when(httpHeaders.getContentType()).thenReturn(MediaType.APPLICATION_JSON);
        when(httpHeaders.getLocation()).thenReturn(null);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toLongString()).thenReturn("test-signature-long-string");
        when(signature.getParameterNames()).thenReturn(new String[]{"studyId", "participantId", "groupId"});
        when(signature.getParameterTypes()).thenReturn(new Class[]{Long.class, Integer.class, Integer.class});
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L, 2, 3});

        auditAspect.recordActivity(joinPoint, responseEntity);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditService,times(1)).record(captor.capture());

        AuditLog auditLog = captor.getValue();

        assertThat(auditLog).isNotNull();
        assertThat(auditLog.getId()).isNull(); //assigned by the DB
        assertThat(auditLog.getCreated()).isNull(); //assigned by the DB
        assertThat(auditLog.getUserId()).isEqualTo("test-user");
        assertThat(auditLog.getStudyId()).isEqualTo(1L);
        assertThat(auditLog.getAction()).isEqualTo("test-signature-long-string");
        assertThat(auditLog.getActionState()).isEqualTo(AuditLog.ActionState.success);
        assertThat(auditLog.getTimestamp()).isNotNull();
        assertThat(auditLog.getResource()).isNull();
        assertThat(auditLog.getDetails()).isNotNull();
        assertThat(auditLog.getDetails().get("param_participantId")).isEqualTo(2);
        assertThat(auditLog.getDetails().get("param_groupId")).isEqualTo(3);
        assertThat(auditLog.getDetails().get("header_content-type")).isEqualTo(MediaType.APPLICATION_JSON_VALUE);

    }
    @Test
    public void testStudyIdFromReturnValue() throws Throwable {

        // Creating mock objects
        JoinPoint joinPoint = Mockito.mock(JoinPoint.class);
        CodeSignature signature = Mockito.mock(CodeSignature.class);
        ResponseEntity<StudyDTO> responseEntity = Mockito.mock(ResponseEntity.class);
        StudyDTO  studyDTO = Mockito.mock(StudyDTO.class);
        HttpHeaders httpHeaders = Mockito.mock(HttpHeaders.class);

        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(responseEntity.getHeaders()).thenReturn(httpHeaders);
        when(responseEntity.getBody()).thenReturn(studyDTO);
        when(httpHeaders.getContentType()).thenReturn(MediaType.APPLICATION_JSON);
        when(httpHeaders.getLocation()).thenReturn(null);
        when(studyDTO.getStudyId()).thenReturn(5L);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toLongString()).thenReturn("test-signature-long-string");
        when(signature.getParameterNames()).thenReturn(new String[]{});
        when(signature.getParameterTypes()).thenReturn(new Class[]{});
        when(joinPoint.getArgs()).thenReturn(new Object[]{});

        auditAspect.recordActivity(joinPoint, responseEntity);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditService,times(1)).record(captor.capture());

        AuditLog auditLog = captor.getValue();

        assertThat(auditLog).isNotNull();
        assertThat(auditLog.getId()).isNull(); //assigned by the DB
        assertThat(auditLog.getCreated()).isNull(); //assigned by the DB
        assertThat(auditLog.getUserId()).isEqualTo("test-user");
        assertThat(auditLog.getStudyId()).isEqualTo(5L);
        assertThat(auditLog.getAction()).isEqualTo("test-signature-long-string");
        assertThat(auditLog.getActionState()).isEqualTo(AuditLog.ActionState.success);
        assertThat(auditLog.getTimestamp()).isNotNull();
        assertThat(auditLog.getResource()).isNull();
        assertThat(auditLog.getDetails()).isNotNull();
        assertThat(auditLog.getDetails().get("header_content-type")).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        assertThat(auditLog.getDetails().get("user_roles")).isEqualTo(List.of(PlatformRole.MORE_VIEWER.name()));
    }

    @Test
    public void test4xxResponseToActionState() throws Throwable {

        // Creating mock objects
        JoinPoint joinPoint = Mockito.mock(JoinPoint.class);
        CodeSignature signature = Mockito.mock(CodeSignature.class);
        ResponseEntity<Void> responseEntity = Mockito.mock(ResponseEntity.class);
        HttpHeaders httpHeaders = Mockito.mock(HttpHeaders.class);

        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(responseEntity.getHeaders()).thenReturn(httpHeaders);
        when(httpHeaders.getContentType()).thenReturn(null);
        when(httpHeaders.getLocation()).thenReturn(null);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toLongString()).thenReturn("test-signature-long-string");
        when(signature.getParameterNames()).thenReturn(new String[]{"studyId"});
        when(signature.getParameterTypes()).thenReturn(new Class[]{Long.class});
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});

        auditAspect.recordActivity(joinPoint, responseEntity);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditService,times(1)).record(captor.capture());

        AuditLog auditLog = captor.getValue();

        assertThat(auditLog).isNotNull();
        assertThat(auditLog.getId()).isNull(); //assigned by the DB
        assertThat(auditLog.getCreated()).isNull(); //assigned by the DB
        assertThat(auditLog.getUserId()).isEqualTo("test-user");
        assertThat(auditLog.getStudyId()).isEqualTo(1L);
        assertThat(auditLog.getAction()).isEqualTo("test-signature-long-string");
        assertThat(auditLog.getActionState()).isEqualTo(AuditLog.ActionState.error);
        assertThat(auditLog.getTimestamp()).isNotNull();
        assertThat(auditLog.getResource()).isNull();
        assertThat(auditLog.getDetails()).isNotNull();
        assertThat(auditLog.getDetails().get("header_content-type")).isNull();
        assertThat(auditLog.getDetails().get("user_roles")).isEqualTo(List.of(PlatformRole.MORE_VIEWER.name()));
    }

    @Test
    public void test5xxResponseToActionState() throws Throwable {
        // Creating mock objects
        JoinPoint joinPoint = Mockito.mock(JoinPoint.class);
        CodeSignature signature = Mockito.mock(CodeSignature.class);
        ResponseEntity<Void> responseEntity = Mockito.mock(ResponseEntity.class);
        HttpHeaders httpHeaders = Mockito.mock(HttpHeaders.class);

        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        when(responseEntity.getHeaders()).thenReturn(httpHeaders);
        when(httpHeaders.getContentType()).thenReturn(null);
        when(httpHeaders.getLocation()).thenReturn(null);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toLongString()).thenReturn("test-signature-long-string");
        when(signature.getParameterNames()).thenReturn(new String[]{"studyId"});
        when(signature.getParameterTypes()).thenReturn(new Class[]{Long.class});
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});

        auditAspect.recordActivity(joinPoint, responseEntity);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditService,times(1)).record(captor.capture());

        AuditLog auditLog = captor.getValue();

        assertThat(auditLog).isNotNull();
        assertThat(auditLog.getId()).isNull(); //assigned by the DB
        assertThat(auditLog.getCreated()).isNull(); //assigned by the DB
        assertThat(auditLog.getUserId()).isEqualTo("test-user");
        assertThat(auditLog.getStudyId()).isEqualTo(1L);
        assertThat(auditLog.getAction()).isEqualTo("test-signature-long-string");
        assertThat(auditLog.getActionState()).isEqualTo(AuditLog.ActionState.error);
        assertThat(auditLog.getTimestamp()).isNotNull();
        assertThat(auditLog.getResource()).isNull();
        assertThat(auditLog.getDetails()).isNotNull();
        assertThat(auditLog.getDetails().get("header_content-type")).isNull();
        assertThat(auditLog.getDetails().get("user_roles")).isEqualTo(List.of(PlatformRole.MORE_VIEWER.name()));
    }

    @Test
    public void test3xxResponseToActionState() throws Throwable {
        // Creating mock objects
        JoinPoint joinPoint = Mockito.mock(JoinPoint.class);
        CodeSignature signature = Mockito.mock(CodeSignature.class);
        ResponseEntity<Void> responseEntity = Mockito.mock(ResponseEntity.class);
        HttpHeaders httpHeaders = Mockito.mock(HttpHeaders.class);

        URI seeOther = new URI("http://see-other.more.at/study/1/other");

        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.SEE_OTHER);
        when(responseEntity.getHeaders()).thenReturn(httpHeaders);
        when(httpHeaders.getContentType()).thenReturn(null);
        when(httpHeaders.getLocation()).thenReturn(seeOther);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toLongString()).thenReturn("test-signature-long-string");
        when(signature.getParameterNames()).thenReturn(new String[]{"studyId"});
        when(signature.getParameterTypes()).thenReturn(new Class[]{Long.class});
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});

        auditAspect.recordActivity(joinPoint, responseEntity);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditService,times(1)).record(captor.capture());

        AuditLog auditLog = captor.getValue();

        assertThat(auditLog).isNotNull();
        assertThat(auditLog.getId()).isNull(); //assigned by the DB
        assertThat(auditLog.getCreated()).isNull(); //assigned by the DB
        assertThat(auditLog.getUserId()).isEqualTo("test-user");
        assertThat(auditLog.getStudyId()).isEqualTo(1L);
        assertThat(auditLog.getAction()).isEqualTo("test-signature-long-string");
        assertThat(auditLog.getActionState()).isEqualTo(AuditLog.ActionState.redirect);
        assertThat(auditLog.getTimestamp()).isNotNull();
        assertThat(auditLog.getResource()).isNull();
        assertThat(auditLog.getDetails()).isNotNull();
        assertThat(auditLog.getDetails().get("header_location")).isEqualTo(seeOther.toString());
        assertThat(auditLog.getDetails().get("user_roles")).isEqualTo(List.of(PlatformRole.MORE_VIEWER.name()));
        }
}
