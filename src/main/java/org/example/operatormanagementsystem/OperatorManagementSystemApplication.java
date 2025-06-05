package org.example.operatormanagementsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "org.example.operatormanagementsystem.repository")
@EntityScan(basePackages = "org.example.operatormanagementsystem.entity")
public class OperatorManagementSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(OperatorManagementSystemApplication.class, args);
    }
}