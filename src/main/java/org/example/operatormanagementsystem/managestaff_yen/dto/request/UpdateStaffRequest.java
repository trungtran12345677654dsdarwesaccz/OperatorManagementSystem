package org.example.operatormanagementsystem.managestaff_yen.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.example.operatormanagementsystem.enumeration.UserGender;
import org.example.operatormanagementsystem.enumeration.UserStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStaffRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 50)
    private String fullName;

    @NotBlank(message = "Username is required")
    @Size(max = 100)
    private String username;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Size(max = 100)
    private String email;

    @Size(max = 20)
    private String phone;

    @Size(max = 255)
    private String address;

    private UserGender gender;
    private UserStatus status;
}
