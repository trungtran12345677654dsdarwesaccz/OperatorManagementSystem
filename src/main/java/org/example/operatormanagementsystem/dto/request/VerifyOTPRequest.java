package org.example.operatormanagementsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter@Setter@Builder
public class VerifyOTPRequest {
    @NotBlank(message = "Email is not blank t send OTP.")
    private String email;
    @NotBlank(message = "OTP is not null to verify account.")
    private String otp;
    private String ip;
    private String userAgent;
    private String deviceInfo;
}
