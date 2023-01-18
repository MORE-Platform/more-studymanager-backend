package io.redlink.more.studymanager.configuration;

import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import io.redlink.more.studymanager.service.StudyPermissionService;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfiguration extends GlobalMethodSecurityConfiguration {

    private final StudyPermissionService studyPermissionService;
    private final OAuth2AuthenticationService oAuth2AuthenticationService;
    public MethodSecurityConfiguration(StudyPermissionService studyPermissionService, OAuth2AuthenticationService oAuth2AuthenticationService){
        this.studyPermissionService = studyPermissionService;
        this.oAuth2AuthenticationService = oAuth2AuthenticationService;
    }

    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        MoreMethodSecurityExpressionHandler expressionHandler =
                new MoreMethodSecurityExpressionHandler(studyPermissionService, oAuth2AuthenticationService);
        expressionHandler.setPermissionEvaluator(new MorePermissionEvaluator());
        return expressionHandler;
    }


    static class MoreMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {

        public MoreMethodSecurityExpressionHandler(StudyPermissionService studyPermissionService, OAuth2AuthenticationService oAuth2AuthenticationService){
            this.studyPermissionService = studyPermissionService;
            this.oAuth2AuthenticationService = oAuth2AuthenticationService;
        }
        private final StudyPermissionService studyPermissionService;
        private final OAuth2AuthenticationService oAuth2AuthenticationService;

        @Override
        protected MethodSecurityExpressionOperations createSecurityExpressionRoot(
                Authentication authentication, MethodInvocation invocation) {
            MoreMethodSecurityExpressionRoot root =
                    new MoreMethodSecurityExpressionRoot(authentication, studyPermissionService, oAuth2AuthenticationService);
            root.setPermissionEvaluator(getPermissionEvaluator());
            root.setTrustResolver(this.getTrustResolver());
            root.setRoleHierarchy(getRoleHierarchy());
            return root;
        }
    }


    static class MoreMethodSecurityExpressionRoot extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {

        private final StudyPermissionService studyPermissionService;
        private final OAuth2AuthenticationService authService;

        public MoreMethodSecurityExpressionRoot(Authentication authentication, StudyPermissionService studyPermissionService, OAuth2AuthenticationService authService) {
            super(authentication);
            this.studyPermissionService = studyPermissionService;
            this.authService = authService;
        }

        public boolean hasAnyRole(Long studyId, String roles) {
            Set<StudyRole> roleSet;
            switch (roles) {
                case "STUDY_READER" -> roleSet = new HashSet<>(Arrays.asList(StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR, StudyRole.STUDY_VIEWER));
                case "STUDY_WRITER" -> roleSet = new HashSet<>(Arrays.asList(StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR));
                case "STUDY_ADMIN" -> roleSet = new HashSet<>() {{
                    add(StudyRole.STUDY_ADMIN);
                }};
                default -> roleSet = new HashSet<>();
            }
            return studyPermissionService.hasAnyRole(studyId, authService.getCurrentUser().id(), roleSet);
        }

        @Override
        public void setFilterObject(Object filterObject) {

        }

        @Override
        public Object getFilterObject() {
            return null;
        }

        @Override
        public void setReturnObject(Object returnObject) {

        }

        @Override
        public Object getReturnObject() {
            return null;
        }

        @Override
        public Object getThis() {
            return this;
        }
    }

    static class MorePermissionEvaluator implements PermissionEvaluator {
        @Override
        public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
            if ((auth == null) || (targetDomainObject == null) || !(permission instanceof String)){
                return false;
            }
            String targetType = targetDomainObject.getClass().getSimpleName().toUpperCase();

            return hasPrivilege(auth, targetType, permission.toString().toUpperCase());
        }

        @Override
        public boolean hasPermission(
                Authentication auth, Serializable targetId, String targetType, Object permission) {
            if ((auth == null) || (targetType == null) || !(permission instanceof String)) {
                return false;
            }
            return hasPrivilege(auth, targetType.toUpperCase(),
                    permission.toString().toUpperCase());
        }

        private boolean hasPrivilege(Authentication auth, String targetType, String permission) {
            for (GrantedAuthority grantedAuth : auth.getAuthorities()) {
                if (grantedAuth.getAuthority().startsWith(targetType) &&
                        grantedAuth.getAuthority().contains(permission)) {
                    return true;
                }
            }
            return false;
        }
    }
}