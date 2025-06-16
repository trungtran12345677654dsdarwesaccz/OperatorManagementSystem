package org.example.operatormanagementsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {
    "org.example.operatormanagementsystem.repository",
    "org.example.operatormanagementsystem.managercustomer.repository"
})
@EntityScan(basePackages = "org.example.operatormanagementsystem.entity")
public class OperatorManagementSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(OperatorManagementSystemApplication.class, args);

        // ⚠️ Mã hóa mật khẩu tại đây
        String rawPassword = "123456";
        String encodedPassword = new BCryptPasswordEncoder().encode(rawPassword);
        System.out.println("🔐 Encoded password for '123456' is:\n" + encodedPassword);
    }
}