package com.studentforge;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Slf4j
@SpringBootApplication
@EnableJpaAuditing
public class StudentForgeApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(StudentForgeApiApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void onReady() {
		log.info("Веб-Интерфейс: http://localhost:8080");
		log.info("Swagger UI:    http://localhost:8080/swagger-ui.html");
		log.info("Health Check:  http://localhost:8080/actuator/health");
	}
}
