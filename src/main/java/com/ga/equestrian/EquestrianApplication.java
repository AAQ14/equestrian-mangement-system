package com.ga.equestrian;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EquestrianApplication {

	public static void main(String[] args) {
		SpringApplication.run(EquestrianApplication.class, args);
	}

}
