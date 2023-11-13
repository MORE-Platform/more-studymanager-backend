/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.model.MoreUser;
import io.redlink.more.studymanager.model.SearchResult;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test-containers-flyway")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate template;

    @BeforeEach
    void deleteAll(){
        template.update("DELETE FROM users");
    }

    @Test
    @DisplayName("User is inserted in database and returned")
    void testInsert(){
        MoreUser user = new MoreUser("123", "name", "inst", "123", Instant.now(), Instant.now());
        MoreUser userResponse = userRepository.save(user);

        assertThat(userResponse.id()).isNotNull();
        assertThat(userResponse.fullName()).isEqualTo(user.fullName());

    }

    @Test
    @DisplayName("User is updated in database and returned")
    void testUpdate(){
        MoreUser user = new MoreUser("123", "name", "inst", "123", Instant.now(), Instant.now());
        MoreUser userResponse = userRepository.save(user);

        MoreUser update = new MoreUser("123", "name", "Institution", "321", Instant.now(), Instant.now());

        MoreUser updated = userRepository.save(update);

        Optional<MoreUser> queried = userRepository.getById(userResponse.id());

        assertThat(queried).isPresent()
                .contains(updated);


        userRepository.deleteById(userResponse.id());
        assertThat(userRepository.getById(userResponse.id()))
                .isEmpty();
    }

    @Test
    void testSearch() {
        var user1 = userRepository.save(new MoreUser("1", "User Eins", "Company", "user1@company.com"));
        var user2 = userRepository.save(new MoreUser("2", "Benutzer Zwei", "Firma", "benutzer2@firma.com"));
        var user3 = userRepository.save(new MoreUser("3", "Usager Trois", "Entreprise", "usager3@entreprise.fr"));

        assertThat(userRepository.findUser("", 0, 10))
                .returns(List.of(), SearchResult::content)
                .returns(0, SearchResult::offset)
                .returns(0L, SearchResult::numFound)
        ;

        assertThat(userRepository.findUser("XXXX", 0, 10))
                .returns(List.of(), SearchResult::content)
                .returns(0, SearchResult::offset)
                .returns(0L, SearchResult::numFound)
        ;

        assertThat(userRepository.findUser("tro", 0, 10))
                .returns(user3, r -> r.content().get(0))
                .returns(0, SearchResult::offset)
                .returns(1L, SearchResult::numFound)
        ;

        assertThat(userRepository.findUser("@", 1, 1))
                .returns(1, r -> r.content().size())
                .returns(user2, r -> r.content().get(0))
                .returns(1, SearchResult::offset)
                .returns(3L, SearchResult::numFound)
        ;

    }
}
