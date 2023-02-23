package io.redlink.more.studymanager.component.observation.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ParticipantData (
    String firstname,
    String lastname,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String token
){
    @Override
    public String toString() {
        return "ParticipantData{" +
                "firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
