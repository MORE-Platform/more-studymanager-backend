/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache License, Version 2.0.
 */
package io.redlink.more.studymanager.component.observation.lime.model;

/**
 * A file a participant uploaded with a response, fetched from LimeSurvey on demand.
 *
 * @param questionCode the code of the file-upload question the file was uploaded for, if LimeSurvey reports it
 * @param filename     the name LimeSurvey stores the file under - the key the response's answer lists
 * @param name         the name the participant's file had
 * @param extension    the file extension, as LimeSurvey recorded it
 * @param content      the file itself; records do not deep-compare it, so do not rely on equality
 */
public record UploadedFile(
        String questionCode,
        String filename,
        String name,
        String extension,
        byte[] content
) {}
