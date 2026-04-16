package com.streamflix.catalog.config;

import com.streamflix.catalog.model.Movie;
import com.streamflix.catalog.repository.MovieRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seedDatabase(MovieRepository repository) {
        return args -> {
            // Only insert if the database is empty so we don't duplicate on restarts
            if (repository.count() == 0) {
                repository.saveAll(List.of(
                        new Movie("Inception", "A thief who steals corporate secrets through the use of dream-sharing technology.", 2010),
                        new Movie("The Matrix", "A computer hacker learns from mysterious rebels about the true nature of his reality.", 1999),
                        new Movie("Interstellar", "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival.", 2014)
                ));
                System.out.println("Mock movies loaded into MongoDB!");
            }
        };
    }
}
