package com.swp.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EntityScan(basePackages = "com.swp.api.model")
public class ApiApplication {

	@Value("${server.port:8080}")
	private String serverPort;

	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}

	@Bean
	public CommandLineRunner showSwaggerUrl() {
		return args -> {
			System.out.println("===============================================");
			System.out.println("Swagger UI is available at:");
			System.out.println("http://localhost:" + serverPort + "/swagger-ui.html");
			System.out.println("===============================================");
		};
	}
}
