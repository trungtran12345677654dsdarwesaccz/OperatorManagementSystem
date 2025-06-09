package org.example.operatormanagementsystem.managercustomer.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {

    @Size(max = 50, message = "Full name must not exceed 50 characters.")
    private String fullName;

    @Size(min = 4, max = 100, message = "Username must be between 4 and 100 characters.")
    private String username;

    @Email(message = "Invalid email format.")
    @Size(max = 100, message = "Email must not exceed 100 characters.")
    private String email;

    @Pattern(regexp = "^(\\+84|0)[0-9]{9}$", message = "Invalid Vietnamese phone number.")
    @Size(max = 20, message = "Phone must not exceed 20 characters.")
    private String phone;

    @Size(max = 255, message = "Address must not exceed 255 characters.")
    private String address;

    @Pattern(regexp = "^(STAFF|MANAGER|CUSTOMER)$", message = "Role must be STAFF, MANAGER, or CUSTOMER.")
    private String role;

    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Gender must be MALE, FEMALE, or OTHER.")
    private String gender;
} 