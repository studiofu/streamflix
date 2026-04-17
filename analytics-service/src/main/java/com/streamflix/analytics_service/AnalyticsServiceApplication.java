package com.streamflix.analytics_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Add @ComponentScan to include the correct package for service beans
import org.springframework.context.annotation.ComponentScan;


@ComponentScan(basePackages = {"com.streamflix.analytics.service"})
@SpringBootApplication
public class AnalyticsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnalyticsServiceApplication.class, args);
	}

}
