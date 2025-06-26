package org.example.operatormanagementsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableJpaRepositories
@EntityScan(basePackages = "org.example.operatormanagementsystem.entity")
public class OperatorManagementSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(OperatorManagementSystemApplication.class, args);


    }
}