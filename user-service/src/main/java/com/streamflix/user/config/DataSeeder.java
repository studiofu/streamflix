package com.streamflix.user.config;

import com.streamflix.user.model.User;
import com.streamflix.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seedDatabase(UserRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.saveAll(List.of(
                        new User("netflix_fan_99", "fan99@streamflix.com"),
                        new User("movie_buff", "buff@streamflix.com")
                ));
                System.out.println("Mock users loaded into PostgreSQL!");
            }
        };
    }
}

