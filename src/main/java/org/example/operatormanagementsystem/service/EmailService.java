package org.example.operatormanagementsystem.service;

import jakarta.mail.MessagingException;
import org.example.operatormanagementsystem.dto.request.VerifyOTPRequest;
import org.example.operatormanagementsystem.dto.response.AuthLoginResponse;
import org.example.operatormanagementsystem.enumeration.UserStatus;

public interface EmailService {
    void sendOTP(String recipient) throws MessagingException;
    AuthLoginResponse verifyOtp(VerifyOTPRequest request);
    void sendStatusChangeNotification(String recipientEmail, UserStatus newStatus) throws MessagingException;
    void sendPasswordResetEmail(String recipientEmail, String resetToken) throws MessagingException;

}
