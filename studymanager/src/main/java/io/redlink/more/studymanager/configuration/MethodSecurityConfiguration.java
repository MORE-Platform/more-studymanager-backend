package io.redlink.more.studymanager.configuration;

import io.redlink.more.studymanager.controller.RequiresStudyRole;
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import io.redlink.more.studymanager.service.StudyPermissionService;
import java.io.Serializable;
import java.util.EnumSet;
import java.util.Set;
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

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfiguration extends GlobalMethodSecurityConfiguration {

    private final StudyPermissionService studyPermissionService;

    public MethodSecurityConfiguration(StudyPermissionService studyPermissionService){
        this.studyPermissionService = studyPermissionService;
    }

    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        MoreMethodSecurityExpressionHandler expressionHandler =
                new MoreMethodSecurityExpressionHandler(studyPermissionService);
        expressionHandler.setPermissionEvaluator(new MorePermissionEvaluator());
        return expressionHandler;
    }


    static class MoreMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {

        public MoreMethodSecurityExpressionHandler(StudyPermissionService studyPermissionService){
            this.studyPermissionService = studyPermissionService;
        }
        private final StudyPermissionService studyPermissionService;

        @Override
        protected MethodSecurityExpressionOperations createSecurityExpressionRoot(
                Authentication authentication, MethodInvocation invocation) {

            final MoreMethodSecurityExpressionRoot root =
                    new MoreMethodSecurityExpressionRoot(authentication, studyPermissionService);
            root.setPermissionEvaluator(getPermissionEvaluator());
            root.setTrustResolver(this.getTrustResolver());
            root.setRoleHierarchy(getRoleHierarchy());

            final RequiresStudyRole annotation = invocation.getMethod().getAnnotation(RequiresStudyRole.class);
            if (annotation != null) {
                var allowedRoles = annotation.value();
                if (allowedRoles != null && allowedRoles.length > 0) {
                    root.setAllowedRoles(Set.of(allowedRoles));
                }
            }

            return root;
        }
    }


    static class MoreMethodSecurityExpressionRoot extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {

        private final StudyPermissionService studyPermissionService;

        private Object filterObject;
        private Object returnObject;
        private Object target;

        private Set<StudyRole> allowedRoles = EnumSet.allOf(StudyRole.class);


        public MoreMethodSecurityExpressionRoot(Authentication authentication, StudyPermissionService studyPermissionService) {
            super(authentication);
            this.studyPermissionService = studyPermissionService;
        }

        public boolean checkStudyRole(Long studyId) {
            return studyPermissionService.hasAnyRole(studyId, getAuthentication().getName(), allowedRoles);
        }

        @Override
        public void setFilterObject(Object filterObject) {
            this.filterObject = filterObject;
        }

        @Override
        public Object getFilterObject() {
            return filterObject;
        }

        @Override
        public void setReturnObject(Object returnObject) {
            this.returnObject = returnObject;
        }

        @Override
        public Object getReturnObject() {
            return returnObject;
        }

        public void setThis(Object target) {
            this.target = target;
        }

        @Override
        public Object getThis() {
            return target;
        }

        public void setAllowedRoles(Set<StudyRole> allowedRoles) {
            this.allowedRoles = allowedRoles;
        }

        public Set<StudyRole> getAllowedRoles() {
            return allowedRoles;
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