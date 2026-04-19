package com.streamflix.user.config;

import com.streamflix.user.model.User;
import com.streamflix.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seedDatabase(UserRepository repository, PasswordEncoder passwordEncoder) {
        return args -> {
            ensureUser(repository, passwordEncoder, "admin", "admin@streamflix.local", "admin");
            ensureUser(repository, passwordEncoder, "netflix_fan_99", "fan99@streamflix.com", "password123");
            ensureUser(repository, passwordEncoder, "movie_buff", "buff@streamflix.com", "password123");
            System.out.println("User bootstrap accounts ensured (admin + demo users).");
        };
    }

    private void ensureUser(
        UserRepository repository,
        PasswordEncoder encoder,
        String username,
        String email,
        String plainPassword
    ) {
        repository.findByUsername(username).ifPresentOrElse(user -> {
            if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
                user.setPasswordHash(encoder.encode(plainPassword));
                repository.save(user);
            }
        }, () -> {
            User user = new User(username, email);
            user.setPasswordHash(encoder.encode(plainPassword));
            repository.save(user);
        });
    }
}

