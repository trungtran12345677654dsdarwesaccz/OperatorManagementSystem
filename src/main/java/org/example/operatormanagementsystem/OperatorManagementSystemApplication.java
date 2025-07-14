package org.example.operatormanagementsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableJpaRepositories
@EntityScan(basePackages = "org.example.operatormanagementsystem.entity")
public class OperatorManagementSystemApplication {
    public static void main(String[] args) {
        String port = System.getenv().getOrDefault("PORT", "8080");
        System.out.println(">>> Using PORT: " + port); // kiểm tra log
        new SpringApplicationBuilder(OperatorManagementSystemApplication.class)
                .properties("server.port=" + port)
                .run(args);

    }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}