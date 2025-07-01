package org.example.operatormanagementsystem.service.impl;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.template.EmailTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailAsyncSender {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String email;

    @Async
    public void sendOTPAsync(String to, String otpCode) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(EmailTemplate.VERIFICATION_CODE_EMAIL.getSubject());
            helper.setText(EmailTemplate.VERIFICATION_CODE_EMAIL.getBody(otpCode), true);
            helper.setFrom(email);
            javaMailSender.send(message);
            System.out.println("DEBUG: sendOTPAsync - OTP email sent to: " + to);
        } catch (Exception e) {
            System.err.println("ERROR: Failed to send OTP email to " + to + ": " + e.getMessage());
        }
    }
}
