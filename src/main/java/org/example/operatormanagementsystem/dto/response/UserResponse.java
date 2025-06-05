package org.example.operatormanagementsystem.dto.response;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class UserResponse {
    private String userName;
    private String address;
    private String fullName;
    private String gender;
    private String password;
    private String email;
    @Enumerated
    private String status;
    @Enumerated
    private String role;
}
