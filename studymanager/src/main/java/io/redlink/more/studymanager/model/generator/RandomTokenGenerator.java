/*
 * Copyright (c) 2022 Redlink GmbH.
 */
package io.redlink.more.studymanager.model.generator;

import org.apache.commons.lang3.RandomStringUtils;

public final class RandomTokenGenerator {

    private static final int TOKEN_LENGTH = 8;
    private static final char[] ALLOWED_CHARS = "ABCDEFGHKLMPRSTUVWXYZ23456789".toCharArray();

    private RandomTokenGenerator() {}

    public static String generate() {
        return RandomStringUtils.random(TOKEN_LENGTH, 0, 0, true, true, ALLOWED_CHARS);
    }

}
