package org.example.operatormanagementsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyOTPRequest {
    @NotBlank(message = "Email is not blank t send OTP.")
    private String email;
    @NotBlank(message = "OTP is not null to verify account.")
    private String otp;
}
