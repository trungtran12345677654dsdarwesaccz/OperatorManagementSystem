package org.example.operatormanagementsystem.customer_thai.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(max = 50, message = "Full name must be less than 50 characters")
    private String fullName;

    @Size(max = 20, message = "Phone number must be less than 20 characters")
    private String phone;

    @Size(max = 255, message = "Address must be less than 255 characters")
    private String address;

    @Size(max = 500, message = "Image URL must be less than 500 characters")
    private String img;
} 