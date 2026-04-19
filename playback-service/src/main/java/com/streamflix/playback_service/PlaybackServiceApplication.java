package com.streamflix.playback_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.streamflix")
@EntityScan(basePackages = "com.streamflix")
@EnableJpaRepositories(basePackages = "com.streamflix.playback.repository")
public class PlaybackServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlaybackServiceApplication.class, args);
	}

}
