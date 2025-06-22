package org.example.operatormanagementsystem.service;

import jakarta.mail.MessagingException;
import org.example.operatormanagementsystem.dto.request.VerifyOTPRequest;
import org.example.operatormanagementsystem.dto.response.AuthLoginResponse;
import org.example.operatormanagementsystem.enumeration.ApprovalStatus;
import org.example.operatormanagementsystem.enumeration.UserStatus;

public interface EmailService {
    void sendOTP(String recipient) throws MessagingException;
    AuthLoginResponse verifyOtp(VerifyOTPRequest request);
    void sendStatusChangeNotification(String recipientEmail, UserStatus newStatus) throws MessagingException;
    void sendPasswordResetEmail(String recipientEmail, String resetToken) throws MessagingException;
    void sendTransportUnitApprovalNotification(String recipientEmail, String userName, String transportUnitName, ApprovalStatus status, String managerNote) throws MessagingException;
    void sendHtmlEmail(String recipientEmail, String subject, String htmlBody) throws MessagingException;
}
