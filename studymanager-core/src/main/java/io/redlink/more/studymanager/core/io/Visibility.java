/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.core.io;

public class Visibility {

    public static final Visibility DEFAULT = new Visibility(true, false);
    boolean changeable;
    boolean hiddenByDefault;

    public Visibility() {
    }

    public Visibility(boolean changeable, boolean hiddenByDefault) {
        this.changeable = changeable;
        this.hiddenByDefault = hiddenByDefault;
    }

    public boolean isChangeable() {
        return changeable;
    }

    public Visibility setChangeable(boolean changeable) {
        this.changeable = changeable;
        return this;
    }

    public boolean isHiddenByDefault() {
        return hiddenByDefault;
    }

    public Visibility setHiddenByDefault(boolean hiddenByDefault) {
        this.hiddenByDefault = hiddenByDefault;
        return this;
    }
}
