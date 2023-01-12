package io.redlink.more.studymanager.controller.security;

import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import io.redlink.more.studymanager.service.StudyPermissionService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import java.util.HashSet;
import java.util.Set;

@Component("controllerPermissionSecurity")
public class ControllerPermissionSecurity{
    private final StudyPermissionService studyPermissionService;

    private final OAuth2AuthenticationService authService;
    public ControllerPermissionSecurity(StudyPermissionService studyPermissionService, OAuth2AuthenticationService authService){
        this.studyPermissionService = studyPermissionService;
        this.authService = authService;
        System.out.println("x");
        System.out.println("x");
        System.out.println("x");
    }

    public boolean hasRoles(final long studyId, String... roles){
        Set<StudyRole> roleSet = new HashSet<>();
        for(String role : roles) {
            switch (role) {
                case "STUDY_VIEWER" -> roleSet.add(StudyRole.STUDY_VIEWER);
                case "STUDY_OPERATOR" -> roleSet.add(StudyRole.STUDY_OPERATOR);
                case "STUDY_ADMIN" -> roleSet.add(StudyRole.STUDY_ADMIN);
            }
        }
        return studyPermissionService.hasAnyRole(studyId, authService.getCurrentUser().id(), roleSet);
    }
}
