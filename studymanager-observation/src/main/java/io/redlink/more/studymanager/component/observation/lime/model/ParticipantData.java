/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.component.observation.lime.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ParticipantData(
        @JsonProperty("participant_info")
        ParticipantInfo participantInfo,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        String token,
        Integer tid
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ParticipantInfo(
            String firstname,
            String lastname,
            String email
    ) {
        public ParticipantInfo(String firstname, String lastname) {
            this(firstname, lastname, null);
        }
    }

    // Convenience accessors so your existing code can keep calling participant.firstname() etc.
    public String firstname() {
        return participantInfo != null ? participantInfo.firstname() : null;
    }

    public String lastname() {
        return participantInfo != null ? participantInfo.lastname() : null;
    }

    @Override
    public String toString() {
        return "ParticipantData{" +
                "firstname='" + firstname() + '\'' +
                ", lastname='" + lastname() + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
