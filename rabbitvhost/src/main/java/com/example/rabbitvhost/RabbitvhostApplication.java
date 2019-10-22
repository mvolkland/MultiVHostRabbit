package com.example.rabbitvhost;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableRabbit
@EnableScheduling
@SpringBootApplication
public class RabbitvhostApplication {

	private static Logger log = LogManager.getLogger();

	public static void main(String[] args) {
		SpringApplication.run(RabbitvhostApplication.class, args);
	}

}
