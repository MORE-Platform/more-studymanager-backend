package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.model.MoreUser;
import java.time.Instant;
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
}
