package io.redlink.more.studymanager.repository;

import io.redlink.more.studymanager.model.MoreUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.sql.Timestamp;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test-containers-flyway")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void deleteAll(){
        userRepository.clear();
    }

    @Test
    @DisplayName("User is inserted in database and returned")
    void testInsert(){
        MoreUser user = new MoreUser("123", "name", "inst", "123", new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()));
        MoreUser userResponse = userRepository.insert(user);

        assertThat(userResponse.id()).isNotNull();
        assertThat(userResponse.name().equals(user.name()));

    }

    @Test
    @DisplayName("User is updated in database and returned")
    void testUpdate(){
        MoreUser user = new MoreUser("123", "name", "inst", "123", new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()));
        MoreUser userResponse = userRepository.insert(user);

        MoreUser update = new MoreUser("123", "name", "Institution", "321", new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()));

        MoreUser updated = userRepository.update(update);

        MoreUser queried = userRepository.getById(userResponse.id());

        assertThat(queried.name()).isEqualTo(updated.name());
        assertThat(queried.id()).isEqualTo(updated.id());

        assertThat(queried.institution()).isEqualTo(updated.institution());

    }
}
