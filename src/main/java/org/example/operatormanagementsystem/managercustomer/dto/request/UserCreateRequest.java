package org.example.operatormanagementsystem.managercustomer.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCreateRequest {

    @NotBlank(message = "Full name cannot be blank.")
    @Size(max = 50, message = "Full name must not exceed 50 characters.")
    private String fullName;

    @NotBlank(message = "Username cannot be blank.")
    @Size(min = 4, max = 100, message = "Username must be between 4 and 100 characters.")
    private String username;

    @Email(message = "Invalid email format.")
    @NotBlank(message = "Email cannot be blank.")
    @Size(max = 100, message = "Email must not exceed 100 characters.")
    private String email;

    @Pattern(regexp = "^(\\+84|0)[0-9]{9}$", message = "Invalid Vietnamese phone number.")
    @Size(max = 20, message = "Phone must not exceed 20 characters.")
    private String phone;

    @Size(max = 255, message = "Address must not exceed 255 characters.")
    private String address;

    @NotBlank(message = "Role cannot be blank.")
    @Pattern(regexp = "^(STAFF|MANAGER|CUSTOMER)$", message = "Role must be STAFF, MANAGER, or CUSTOMER.")
    private String role;

    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Gender must be MALE, FEMALE, or OTHER.")
    private String gender;

    @NotBlank(message = "Password cannot be blank.")
    @Size(min = 6, message = "Password must be at least 6 characters long.")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).*$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, and one number."
    )
    private String password;
}