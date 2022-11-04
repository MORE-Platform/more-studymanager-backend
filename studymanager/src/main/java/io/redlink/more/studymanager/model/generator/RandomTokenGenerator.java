/*
 * Copyright (c) 2022 Redlink GmbH.
 */
package io.redlink.more.mmb.backend.model.generator;

import java.io.Serializable;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

public class RandomTokenGenerator implements IdentifierGenerator {

    private static final int TOKEN_LENGTH = 8;
    private static final char[] ALLOWED_CHARS = "ABCDEFGHKLMPRSTUVWXYZ23456789".toCharArray();

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        return RandomStringUtils.random(TOKEN_LENGTH, 0, 0, true, true, ALLOWED_CHARS);
    }

}
