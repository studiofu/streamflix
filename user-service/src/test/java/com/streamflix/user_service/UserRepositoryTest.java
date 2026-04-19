package com.streamflix.user_service;

import com.streamflix.user.model.User;
import com.streamflix.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void clean() {
        userRepository.deleteAll();
    }

    @Test
    void saveAndFindByUsername() {
        User u = new User("alice", "alice@example.com");
        u.setPasswordHash("hash");
        userRepository.save(u);

        assertThat(userRepository.findByUsername("alice")).isPresent();
        assertThat(userRepository.findByUsername("alice").get().getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void existsByUsername() {
        assertThat(userRepository.existsByUsername("bob")).isFalse();

        User u = new User("bob", "bob@example.com");
        u.setPasswordHash("hash");
        userRepository.save(u);

        assertThat(userRepository.existsByUsername("bob")).isTrue();
        assertThat(userRepository.existsByUsername("carol")).isFalse();
    }
}
