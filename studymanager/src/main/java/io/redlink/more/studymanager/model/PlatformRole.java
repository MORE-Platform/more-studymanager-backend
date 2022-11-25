/*
 * Copyright (c) 2022 Redlink GmbH.
 */
package io.redlink.more.studymanager.model;

/**
 * Roles a user can have on the global/platform level
 */
public enum PlatformRole {
    /**
     * Can <em>view existing</em> studies (where listed in the ACL)
     */
    MORE_VIEWER,
    /**
     * Can <em>create new</em> studies
     */
    MORE_OPERATOR,
    /**
     * Can <em>manage collaborators</em> on all studies
     */
    MORE_ADMIN,

    ;
}
