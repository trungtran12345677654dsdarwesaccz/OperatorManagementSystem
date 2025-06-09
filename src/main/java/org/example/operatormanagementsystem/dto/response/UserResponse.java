package org.example.operatormanagementsystem.dto.response;
import jakarta.persistence.Enumerated;
import lombok.Data;
import org.example.operatormanagementsystem.enumeration.UserRole;
import org.example.operatormanagementsystem.enumeration.UserStatus;

@Data
public class UserResponse {
    private String userName;
    private String address;
    private String fullName;
    private String gender;
    private String password;
    private String email;
    @Enumerated
    private UserStatus status;
    @Enumerated
    private UserRole role;
}
