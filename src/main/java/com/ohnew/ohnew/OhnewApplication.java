package com.ohnew.ohnew;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class OhnewApplication {

	public static void main(String[] args) {
		SpringApplication.run(OhnewApplication.class, args);
	}

}
