package org.example.operatormanagementsystem.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.config.JwtUtil;
import org.example.operatormanagementsystem.dto.request.VerifyOTPRequest;
import org.example.operatormanagementsystem.dto.response.AuthLoginResponse;
import org.example.operatormanagementsystem.entity.Otp; // Import Otp entity
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.enumeration.UserStatus;
import org.example.operatormanagementsystem.repository.OTPVerificationRepository;
import org.example.operatormanagementsystem.repository.UserRepository;
import org.example.operatormanagementsystem.service.EmailService;
import org.example.operatormanagementsystem.template.EmailTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender javaMailSender;
    private final OTPVerificationRepository otpVerificationRepository;
    private final UserRepository userRepository;
    @Value("${spring.mail.username}")
    private String email;
    private final JwtUtil jwtUtil;

    @Transactional
    @Async
    @Override
    public void sendOTP(String recipient) throws MessagingException {

        Optional<Otp> otpVerification = otpVerificationRepository.findByEmail(recipient);

            otpVerificationRepository.delete(otpVerification.get());


        String otpCode = generateOTPEmail();
        System.out.println("DEBUG: sendOTP - Generated OTP code: " + otpCode);

        Otp verification = new Otp();
        verification.setEmail(recipient);
        verification.setOtp(otpCode);
        verification.setExpiredTime(LocalDateTime.now().plusMinutes(1));
        verification.setStatus(Otp.OtpStatus.PENDING);

        Optional<Users> userOptional = userRepository.findByEmail(recipient);
        Users user = userOptional
                .orElseThrow(() -> new UsernameNotFoundException("User not found for OTP generation: " + recipient));
        verification.setUsers(user);

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        String html = EmailTemplate.VERIFICATION_CODE_EMAIL.getBody(otpCode);

        helper.setTo(recipient);
        helper.setSubject(EmailTemplate.VERIFICATION_CODE_EMAIL.getSubject());
        helper.setText(html, true);
        helper.setFrom(email);

        javaMailSender.send(message);
        otpVerificationRepository.save(verification);
        System.out.println("DEBUG: sendOTP - OTP saved to DB and email send attempt completed for '" + recipient + "'");
    }

    @Override
    @Transactional
    public AuthLoginResponse verifyOtp(VerifyOTPRequest request) {
        String userEmail = request.getEmail();
        String otpCode = request.getOtp();

        // 1. Tìm OTP trong cơ sở dữ liệu
        Otp otpRecord = otpVerificationRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BadCredentialsException("OTP not found for this email."));

        // Kiểm tra trạng thái hiện tại của OTP (chỉ cho phép xác minh nếu đang PENDING)
        if (otpRecord.getStatus() != Otp.OtpStatus.PENDING) {
            throw new BadCredentialsException("OTP is not active or has been used/expired.");
        }

        // 2. Kiểm tra OTP có hợp lệ và chưa hết hạn không
        if (!otpRecord.getOtp().equals(otpCode)) {
            // Cập nhật trạng thái OTP thành USED hoặc EXPIRED nếu sai, tùy logic
            otpRecord.setStatus(Otp.OtpStatus.USED); // Đánh dấu là đã sử dụng (sai)
            otpVerificationRepository.save(otpRecord);
            throw new BadCredentialsException("Invalid OTP.");
        }
        if (otpRecord.getExpiredTime().isBefore(LocalDateTime.now())) {
            // Cập nhật trạng thái OTP thành EXPIRED
            otpRecord.setStatus(Otp.OtpStatus.EXPIRED);
            otpVerificationRepository.save(otpRecord);
            throw new BadCredentialsException("OTP has expired.");
        }

        // 3. Nếu OTP hợp lệ và chưa hết hạn, cập nhật trạng thái OTP thành VERIFIED
        otpRecord.setStatus(Otp.OtpStatus.VERIFIED);
        otpVerificationRepository.save(otpRecord); // Lưu lại trạng thái đã xác nhận

        // 4. Lấy thông tin người dùng
        Users user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found after OTP verification."));

        // Cập nhật trạng thái người dùng thành ACTIVE nếu cần
        if (user.getStatus() != UserStatus.ACTIVE) {
            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user); // Lưu thay đổi trạng thái người dùng
        }

        // 5. Kiểm tra lại trạng thái tài khoản (đảm bảo đã ACTIVE cho bước đăng nhập)
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadCredentialsException("Account is not active. Please ensure your email is verified.");
        }

        // 6. Tạo JWT token
        String token = jwtUtil.generateToken(user);
        String role = user.getAuthorities().stream()
                .findFirst()
                .map(Object::toString)
                .orElse("USER");

        // 7. Trả về AuthLoginResponse
        AuthLoginResponse authLoginResponse = new AuthLoginResponse();
        authLoginResponse.setAccessToken(token);
        authLoginResponse.setRole(role);
        return authLoginResponse;
    }

    private String generateOTPEmail() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}
