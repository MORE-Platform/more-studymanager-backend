/*
 * Copyright (c) 2023 Redlink GmbH.
 */
package io.redlink.more.studymanager.controller;

import io.redlink.more.studymanager.model.StudyRole;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.access.prepost.PreAuthorize;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("checkStudyRole(#studyId)")
public @interface RequiresStudyRole {

    StudyRole[] value() default {};

}
