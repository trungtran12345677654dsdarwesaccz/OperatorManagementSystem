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
        // --- Bắt đầu Debug Log ---
        System.out.println("DEBUG: sendOTP - Received recipient email (raw): '" + recipient + "'");
        System.out.println("DEBUG: sendOTP - Length of raw recipient email: " + recipient.length());
        // --- Kết thúc Debug Log ---

        // Làm sạch email: cắt khoảng trắng và chuyển về chữ thường
        String cleanRecipient = recipient.trim().toLowerCase();

        // --- Bắt đầu Debug Log ---
        System.out.println("DEBUG: sendOTP - Cleaned recipient email: '" + cleanRecipient + "'");
        System.out.println("DEBUG: sendOTP - Length of cleaned recipient email: " + cleanRecipient.length());
        // --- Kết thúc Debug Log ---

        // Bước 1: Kiểm tra xem người dùng có tồn tại không
        // Ném UsernameNotFoundException nếu người dùng không được tìm thấy.
        System.out.println("DEBUG: sendOTP - Attempting to find user by cleaned email: '" + cleanRecipient + "'");
        Users user = userRepository.findByEmail(cleanRecipient) // SỬ DỤNG cleanRecipient
                .orElseThrow(() -> new UsernameNotFoundException("User not found for OTP generation: " + cleanRecipient));
        System.out.println("DEBUG: sendOTP - User FOUND! Email: '" + user.getEmail() + "', ID: " + user.getId() + ", Status: " + user.getStatus());
        System.out.println("DEBUG: sendOTP - Length of FOUND user email from DB: " + user.getEmail().length());

        // Bước 2: Xóa OTP cũ nếu tồn tại
        System.out.println("DEBUG: sendOTP - Attempting to find existing OTP for: '" + cleanRecipient + "'");
        Optional<Otp> otpVerification = otpVerificationRepository.findByEmail(cleanRecipient); // SỬ DỤNG cleanRecipient
        if (otpVerification.isPresent()) {
            System.out.println("DEBUG: sendOTP - Existing OTP found for '" + cleanRecipient + "'. Deleting it.");
            otpVerificationRepository.delete(otpVerification.get());
        } else {
            System.out.println("DEBUG: sendOTP - No existing OTP found for '" + cleanRecipient + "'.");
        }

        // Bước 3: Tạo OTP mới
        String otpCode = generateOTPEmail();
        System.out.println("DEBUG: sendOTP - Generated OTP code: " + otpCode + " for " + cleanRecipient);

        Otp verification = new Otp();
        verification.setEmail(cleanRecipient); // SỬ DỤNG cleanRecipient
        verification.setOtp(otpCode); // Đảm bảo trường này trong Otp entity là 'otpCode'
        verification.setCreatedDate(LocalDateTime.now());
        verification.setExpiredTime(LocalDateTime.now().plusMinutes(1));
        verification.setStatus(Otp.OtpStatus.PENDING);
        verification.setUsers(user); // Liên kết OTP với Users entity

        // Bước 4: Gửi email
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        // Đảm bảo EmailTemplate.VERIFICATION_CODE_EMAIL được định nghĩa và có getBody/getSubject
        // Bạn cần chắc chắn class EmailTemplate tồn tại và có các phương thức này
        String html = EmailTemplate.VERIFICATION_CODE_EMAIL.getBody(otpCode);

        helper.setTo(cleanRecipient); // SỬ DỤNG cleanRecipient
        helper.setSubject(EmailTemplate.VERIFICATION_CODE_EMAIL.getSubject());
        helper.setText(html, true);
        helper.setFrom(email); // Sử dụng biến đã inject

        javaMailSender.send(message);
        otpVerificationRepository.save(verification);
        System.out.println("DEBUG: sendOTP - OTP saved to DB and email send attempt completed for '" + cleanRecipient + "'");
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
    @Async
    @Override
    public void sendStatusChangeNotification(String recipientEmail, UserStatus newStatus) throws MessagingException {
        System.out.println("DEBUG: sendStatusChangeNotification - Attempting to send notification to: '" + recipientEmail + "' for status: " + newStatus.name());

        String subject;
        String body;

        switch (newStatus) {
            case ACTIVE:
                subject = "[OperatorManagementSystem] Tài khoản của bạn đã được kích hoạt";
                body = "Chào bạn,<br><br>"
                        + "Tài khoản của bạn với email <b>" + recipientEmail + "</b> đã được quản lý duyệt và kích hoạt thành công. "
                        + "Bạn hiện có thể đăng nhập vào hệ thống của chúng tôi.<br><br>"
                        + "Trân trọng,<br>Đội ngũ OperatorManagementSystem.";
                break;
            case INACTIVE:
                subject = "[OperatorManagementSystem] Thông báo: Trạng thái tài khoản của bạn đã thay đổi";
                body = "Chào bạn,<br><br>"
                        + "Tài khoản của bạn với email <b>" + recipientEmail + "</b> đã được cập nhật trạng thái thành <b>Vô hiệu hóa</b> (Inactive).<br>"
                        + "Nếu bạn tin đây là một sai sót hoặc muốn kích hoạt lại, vui lòng liên hệ với bộ phận hỗ trợ.<br><br>"
                        + "Trân trọng,<br>Đội ngũ OperatorManagementSystem.";
                break;
            case REJECTED:
                subject = "[OperatorManagementSystem] Thông báo: Yêu cầu tài khoản của bạn đã bị từ chối";
                body = "Chào bạn,<br><br>"
                        + "Yêu cầu thay đổi trạng thái của tài khoản <b>" + recipientEmail + "</b> của bạn đã bị quản lý từ chối.<br>"
                        + "Tài khoản của bạn hiện đang ở trạng thái <b>Bị từ chối</b>. Vui lòng liên hệ hỗ trợ nếu có thắc mắc.<br><br>"
                        + "Trân trọng,<br>Đội ngũ OperatorManagementSystem.";
                break;
            case PENDING_APPROVAL:
                subject = "[OperatorManagementSystem] Tài khoản của bạn đang chờ duyệt";
                body = "Chào bạn,<br><br>"
                        + "Tài khoản của bạn với email <b>" + recipientEmail + "</b> đã được tạo thành công và đang ở trạng thái <b>Chờ duyệt</b> (Pending Approval). "
                        + "Vui lòng chờ quản lý xem xét và kích hoạt tài khoản của bạn. Bạn sẽ nhận được email thông báo khi trạng thái thay đổi.<br><br>"
                        + "Trân trọng,<br>Đội ngũ OperatorManagementSystem.";
                break;
            default:
                subject = "[OperatorManagementSystem] Cập nhật trạng thái tài khoản của bạn";
                body = "Chào bạn,<br><br>"
                        + "Tài khoản của bạn với email <b>" + recipientEmail + "</b> đã được cập nhật trạng thái thành: <b>" + newStatus.name() + "</b>.<br><br>"
                        + "Trân trọng,<br>Đội ngũ OperatorManagementSystem.";
                break;
        }

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(recipientEmail);
        helper.setSubject(subject);
        helper.setText(body, true);
        helper.setFrom(email); // Sử dụng email người gửi đã được inject

        System.out.println("DEBUG: sendStatusChangeNotification - Preparing to send email:");
        System.out.println("DEBUG: To: " + recipientEmail);
        System.out.println("DEBUG: From: " + email);
        System.out.println("DEBUG: Subject: " + subject);
        // System.out.println("DEBUG: Body: " + body); // Không nên in toàn bộ body nếu nó quá dài hoặc chứa thông tin nhạy cảm

        javaMailSender.send(message);
        System.out.println("DEBUG: sendStatusChangeNotification - Email sent successfully to '" + recipientEmail + "' for status: " + newStatus.name());
    }


    // Phương thức mới: Gửi email đặt lại mật khẩu
    @Async
    @Override
    public void sendPasswordResetEmail(String recipientEmail, String resetToken) throws MessagingException {
        System.out.println("DEBUG: sendPasswordResetEmail - Attempting to send password reset email to: '" + recipientEmail + "'");

        String resetLink = "http://localhost:8083/reset-password?token=" + resetToken; // <-- THAY THẾ BẰNG URL FRONTEND CỦA BẠN

        String subject = "[OperatorManagementSystem] Yêu cầu đặt lại mật khẩu";
        String body = "Chào bạn,<br><br>"
                + "Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn. "
                + "Vui lòng nhấp vào liên kết sau để đặt lại mật khẩu của bạn: "
                + "<a href=\"" + resetLink + "\">Đặt lại mật khẩu</a><br><br>"
                + "Liên kết này sẽ hết hạn trong 15 phút. Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.<br><br>"
                + "Trân trọng,<br>Đội ngũ OperatorManagementSystem.";

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(recipientEmail);
        helper.setSubject(subject);
        helper.setText(body, true);
        helper.setFrom(email);

        System.out.println("DEBUG: sendPasswordResetEmail - Preparing to send email:");
        System.out.println("DEBUG: To: " + recipientEmail);
        System.out.println("DEBUG: From: " + email);
        System.out.println("DEBUG: Subject: " + subject);
        System.out.println("DEBUG: Reset Link: " + resetLink);

        javaMailSender.send(message);
        System.out.println("DEBUG: sendPasswordResetEmail - Password reset email sent successfully to '" + recipientEmail + "'");
    }
}
