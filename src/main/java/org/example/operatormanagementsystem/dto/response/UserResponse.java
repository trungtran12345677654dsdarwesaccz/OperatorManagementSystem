package org.example.operatormanagementsystem.dto.response;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class UserResponse {
    private String fullname;
    private String password;
    private String email;
    @Enumerated
    private String status;
    private String role;
}