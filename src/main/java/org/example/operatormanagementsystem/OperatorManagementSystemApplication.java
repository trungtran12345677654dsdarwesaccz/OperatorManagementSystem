package org.example.operatormanagementsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {
        "org.example.operatormanagementsystem.repository",
        "org.example.operatormanagementsystem.managestaff_yen.repository",
        "org.example.operatormanagementsystem.ManageHungBranch.repository",
        "org.example.operatormanagementsystem.transportunit.repository",
        "org.example.operatormanagementsystem.managecustomerorderbystaff.repository"
})
@EntityScan(basePackages = "org.example.operatormanagementsystem.entity")

@EnableScheduling
public class OperatorManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(OperatorManagementSystemApplication.class, args);

        // Mã hóa password demo
        String rawPassword = "123456";
        String encodedPassword = new BCryptPasswordEncoder().encode(rawPassword);
        System.out.println("🔐 Encoded password for '123456' is:\n" + encodedPassword);
    }
}
