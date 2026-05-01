package com.apimonitor.ambackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AmbackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AmbackendApplication.class, args);
	}

}
