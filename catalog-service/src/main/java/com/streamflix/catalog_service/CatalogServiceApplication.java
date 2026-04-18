package com.streamflix.catalog_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.cache.annotation.EnableCaching;


// Marks this class as a Spring Boot application and specifies the packages to scan
// for Spring Data MongoDB repositories
@SpringBootApplication(scanBasePackages = "com.streamflix")
@EnableMongoRepositories(basePackages = "com.streamflix")
@EnableCaching // <--- ACTIVATES REDIS CACHING
public class CatalogServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CatalogServiceApplication.class, args);
	}

}
