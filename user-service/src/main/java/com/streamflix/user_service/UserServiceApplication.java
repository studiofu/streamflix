package com.streamflix.user_service;

import com.streamflix.user.model.User;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.streamflix")
//@EntityScan(basePackageClasses = User.class)
@EntityScan(basePackages = "com.streamflix")
@EnableJpaRepositories(basePackages = "com.streamflix.user.repository")
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}

}
