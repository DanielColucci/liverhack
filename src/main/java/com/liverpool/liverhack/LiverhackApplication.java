package com.liverpool.liverhack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class LiverhackApplication {

	public static void main(String[] args) {
		SpringApplication.run(LiverhackApplication.class, args);
	}

}
