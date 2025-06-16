package org.example.operatormanagementsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableScheduling
public class OperatorManagementSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(OperatorManagementSystemApplication.class, args);
    }
}