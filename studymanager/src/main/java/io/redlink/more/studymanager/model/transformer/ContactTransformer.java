package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.ContactDTO;
import io.redlink.more.studymanager.model.Contact;

public class ContactTransformer {

    public static ContactDTO toContactDTO_V1(Contact contact) {
        return new ContactDTO()
                .institute(contact.getInstitute())
                .person(contact.getPerson())
                .email(contact.getEmail())
                .phoneNumber(contact.getPhoneNumber());
    }

    public static Contact fromContactDTO_V1(ContactDTO contactDTO) {
        return new Contact()
                .setInstitute(contactDTO.getInstitute())
                .setPerson(contactDTO.getPerson())
                .setEmail(contactDTO.getEmail())
                .setPhoneNumber(contactDTO.getPhoneNumber());
    }
}
