package io.redlink.more.studymanager.audit;

import io.redlink.more.studymanager.api.v1.model.StudyDTO;
import io.redlink.more.studymanager.model.AuthenticatedUser;
import io.redlink.more.studymanager.model.PlatformRole;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.audit.AuditLog;
import io.redlink.more.studymanager.properties.AuditProperties;
import io.redlink.more.studymanager.service.AuditService;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import io.redlink.more.studymanager.utils.MapperUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Aspect
@Component
public class AuditAspect {
    private final AuditProperties auditProperties;
    private final AuditService auditService;
    private final OAuth2AuthenticationService authService;

    public AuditAspect(
            AuditProperties auditProperties,
            AuditService auditService,
            OAuth2AuthenticationService authService) {
        this.auditService = auditService;
        this.authService = authService;
        this.auditProperties = auditProperties;
    }
        @AfterThrowing(pointcut = "@annotation(Audited)", throwing = "e")
        public void recordException(JoinPoint joinPoint, Throwable e){
            auditService.record(toAuditLog(authService.getCurrentUser(), joinPoint, null, e));
        }
        @AfterReturning(pointcut = "@annotation(Audited)", returning = "returnValue")
        public void recordActivity(JoinPoint joinPoint, Object returnValue) throws Throwable {
            auditService.record(toAuditLog(authService.getCurrentUser(), joinPoint, returnValue, null));
        }

    private AuditLog toAuditLog(AuthenticatedUser user, JoinPoint joinPoint, Object returnValue, Throwable e) {
        Instant timestamp = Instant.now();
        String action = joinPoint.getSignature().toLongString();
        CodeSignature methodSignature = (CodeSignature) joinPoint.getSignature();
        //we need to search for the user and study
        Map<String, Object> parameters = new HashMap<>();
        int studyParamIdx = -1;
        for(int i=0; i < methodSignature.getParameterNames().length; i++){
            var paramName = methodSignature.getParameterNames()[i];
            var paramType = methodSignature.getParameterTypes()[i];
            if(studyParamIdx < 0 &&
                    paramName.toLowerCase(Locale.ROOT).startsWith("study") &&
                    paramType.isAssignableFrom(Long.class)){
                studyParamIdx = i;
            } else if(MapperUtils.isSerializable(joinPoint.getArgs()[i], auditProperties.detailsByteLimit())){
                parameters.put("param_" + paramName, joinPoint.getArgs()[i]);
            } else {
                parameters.put("param_" + paramName, "<not-serializable>");
            }
        }
        final Long studyId;
        if(studyParamIdx < 0){
            studyId = getStudyIdFromResponse(returnValue);
        } else {
            studyId = (Long) joinPoint.getArgs()[studyParamIdx];
        }
        if(studyId == null){
            throw new IllegalStateException(String.format("Unable to create AuditLog for Action '%s' as studyId parameter could not be retrieved from the signature", action));
        }

        AuditLog auditLog = new AuditLog(user.id(), studyId, action, timestamp)
                .setActionState(getActionState(returnValue, e));
        //finally set some additional information (if available)
        auditLog.getDetails().putAll(parameters);
        auditLog.getDetails().put("user_roles", user.roles().stream().map(PlatformRole::name).toList());
        if(e != null){
            auditLog.getDetails().put("exception", e.getClass().getName());
        }
        if(returnValue instanceof ResponseEntity re){
            auditLog.getDetails().put("http_status", re.getStatusCode().value());
            if(re.getHeaders().getContentType() != null) {
                auditLog.getDetails().put("header_content-type", re.getHeaders().getContentType().toString());
            }
            if(re.getHeaders().getLocation() != null) {
                auditLog.getDetails().put("header_location", re.getHeaders().getLocation().toString());
            }
        }
        return auditLog;
    }

    /**
     * Tries to get the StudyId from the method response ({@link JoinPoint#getTarget()}). This is usefull e.g. for
     * a method the imports a study to the system
     * @param target the {@link JoinPoint#getTarget()}
     * @return the StudyId or <code>null</code> if not successfull
     */
    private static Long getStudyIdFromResponse(Object target) {
        Object response = (target instanceof ResponseEntity re) && re.getStatusCode().is2xxSuccessful() ? re.getBody() : target;
        if(response instanceof StudyDTO studyDTO){
            return studyDTO.getStudyId();
        } else if(response instanceof Study study){
            return study.getStudyId();
        } else {
            return null;
        }
    }

    private static AuditLog.ActionState getActionState(Object target, Throwable e) {
        final AuditLog.ActionState state;
        if(e != null){
            state = AuditLog.ActionState.error;
        } else if(target instanceof ResponseEntity re){
            //special support for Methods that return ResponseEntity
            if(re.getStatusCode().is2xxSuccessful()){
                state = AuditLog.ActionState.success;
            } else if(re.getStatusCode().is3xxRedirection()){
                state = AuditLog.ActionState.redirect;
            } else if( re.getStatusCode().is4xxClientError() || re.getStatusCode().is5xxServerError()){
                state = AuditLog.ActionState.error;
            } else {
                state = AuditLog.ActionState.unknown;
            }
        } else if(target == null){
            state = AuditLog.ActionState.error;
        } else {
            state = AuditLog.ActionState.success;
        }
        return state;
    }

}
