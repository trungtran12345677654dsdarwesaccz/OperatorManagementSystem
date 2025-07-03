package org.example.operatormanagementsystem.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.config.JwtUtil;
import org.example.operatormanagementsystem.dto.request.LoginRequest;
import org.example.operatormanagementsystem.dto.request.VerifyOTPRequest;
import org.example.operatormanagementsystem.dto.response.AuthLoginResponse;
import org.example.operatormanagementsystem.entity.*;
import org.example.operatormanagementsystem.enumeration.UserRole;
import org.example.operatormanagementsystem.enumeration.UserStatus;
import org.example.operatormanagementsystem.repository.*;
import org.example.operatormanagementsystem.service.EmailService;
import org.example.operatormanagementsystem.service.UserActivityLogService;
import org.example.operatormanagementsystem.template.EmailTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.example.operatormanagementsystem.enumeration.ApprovalStatus;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender javaMailSender;
    private final OTPVerificationRepository otpVerificationRepository;
    private final UserRepository userRepository;
    @Value("${spring.mail.username}")
    private String email;
    private final JwtUtil jwtUtil;
    private final EmailAsyncSender emailAsyncSender;
    private final LoginHistoryRepository loginHistoryRepository;
    private final UserSessionRepository userSessionRepository;
    private final UserActivityLogService userActivityLogService;
private final UserUsageStatRepository usageStatRepository;
    @Override
    @Transactional
    public void sendOTP(String recipient) {
        String cleanRecipient = recipient.trim();

        Users user = userRepository.findByEmail(cleanRecipient)
                .orElseThrow(() -> new UsernameNotFoundException("User not found for OTP generation: " + cleanRecipient));

        otpVerificationRepository.findByEmail(cleanRecipient)
                .ifPresent(otpVerificationRepository::delete);

        String otpCode = generateOTPEmail();
        System.out.println("DEBUG: sendOTP - Generated OTP code: " + otpCode + " for " + cleanRecipient);

        Otp verification = new Otp();
        verification.setEmail(cleanRecipient);
        verification.setOtp(otpCode);
        verification.setCreatedDate(LocalDateTime.now());
        verification.setExpiredTime(LocalDateTime.now().plusMinutes(5));
        verification.setStatus(Otp.OtpStatus.PENDING);
        verification.setUsers(user);

        otpVerificationRepository.save(verification);

        emailAsyncSender.sendOTPAsync(cleanRecipient, otpCode);
    }


    @Override
    @Transactional
    public AuthLoginResponse verifyOtp(VerifyOTPRequest request) {
        String userEmail = request.getEmail();
        String otpCode = request.getOtp();

        Otp otpRecord = otpVerificationRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BadCredentialsException("OTP not found for this email."));

        if (otpRecord.getStatus() != Otp.OtpStatus.PENDING) {
            throw new BadCredentialsException("OTP is not active or has been used/expired.");
        }

        if (otpRecord.getExpiredTime().isBefore(LocalDateTime.now())) {
            otpRecord.setStatus(Otp.OtpStatus.EXPIRED);
            otpVerificationRepository.save(otpRecord);
            throw new BadCredentialsException("OTP has expired.");
        }

        if (!otpRecord.getOtp().equals(otpCode)) {
            otpRecord.setStatus(Otp.OtpStatus.USED);
            otpVerificationRepository.save(otpRecord);
            throw new BadCredentialsException("Invalid OTP.");
        }


        otpRecord.setStatus(Otp.OtpStatus.VERIFIED);
        otpVerificationRepository.save(otpRecord);

        Users user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found after OTP verification."));

        if (user.getStatus() != UserStatus.ACTIVE) {
            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user);
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadCredentialsException("Account is not active. Please ensure your email is verified.");
        }

        String token;
        UserRole assignedRole = null;

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        if (authorities != null && !authorities.isEmpty()) {
            String roleStringFromAuthority = authorities.iterator().next().getAuthority();
            if (roleStringFromAuthority.startsWith("ROLE_")) {
                roleStringFromAuthority = roleStringFromAuthority.substring(5);
            }

            try {
                UserRole potentialRole = UserRole.valueOf(roleStringFromAuthority.toUpperCase());

                if (potentialRole == UserRole.MANAGER || potentialRole == UserRole.STAFF) {
                    assignedRole = potentialRole;
                } else {
                    System.err.println("Attempted login with disallowed role: '" + potentialRole + "' for user " + user.getUsername());
                    throw new InsufficientAuthenticationException("Access denied: Only MANAGER and STAFF roles are allowed.");
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid role string found from user authorities: " + roleStringFromAuthority + ". For user: " + user.getUsername());
                throw new InsufficientAuthenticationException("Access denied: Invalid user role.");
            }
        } else {
            System.err.println("User " + user.getUsername() + " has no assigned authorities.");
            throw new InsufficientAuthenticationException("Access denied: User has no assigned roles.");
        }

        if (assignedRole == null) {
            System.err.println("Unexpected state: assignedRole is null after processing for user " + user.getUsername());
            throw new InsufficientAuthenticationException("Access denied: Role could not be determined.");
        }
        List<UserSession> oldSessions = userSessionRepository.findByUserAndActiveTrue(user);
        for (UserSession old : oldSessions) {
            old.setActive(false);
        }
        userSessionRepository.saveAll(oldSessions);

        token = jwtUtil.generateToken(user);
        saveUserSession(user, token, request);
        saveLoginHistory(user, request);
        userActivityLogService.log(
                user,
                "LOGIN",
                "Đăng nhập"
        );
        UserUsageStat stat = usageStatRepository.findByUser(user)
                .orElseGet(() -> {
                    UserUsageStat s = new UserUsageStat();
                    s.setUser(user);
                    s.setCurrentDate(LocalDate.now());
                    return s;
                });

        stat.setLoginCount(stat.getLoginCount() + 1);
        stat.setLastLoginAt(LocalDateTime.now());
        usageStatRepository.save(stat);



        AuthLoginResponse authLoginResponse = new AuthLoginResponse();
        authLoginResponse.setAccessToken(token);
        authLoginResponse.setRole(assignedRole);
        return authLoginResponse;
    }

    private void saveUserSession(Users user, String token, VerifyOTPRequest request) {
        UserSession session = UserSession.builder()
                .token(token)
                .user(user)
                .ipAddress(request.getIp())
                .userAgent(request.getUserAgent()) // truyền từ client
                .deviceInfo(request.getDeviceInfo()) // gợi ý: dùng user-agent-parser
                .createdAt(LocalDateTime.now())
                .lastAccessedAt(LocalDateTime.now())
                .active(true)
                .build();
        userSessionRepository.save(session);
    }

    private void saveLoginHistory(Users user, VerifyOTPRequest request) {
        LoginHistory history = LoginHistory.builder()
                .user(user)
                .ipAddress(request.getIp())
                .userAgent(request.getUserAgent())
                .loginTime(LocalDateTime.now())
                .build();
        loginHistoryRepository.save(history);
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
        helper.setFrom(email);

        System.out.println("DEBUG: sendStatusChangeNotification - Preparing to send email:");
        System.out.println("DEBUG: To: " + recipientEmail);
        System.out.println("DEBUG: From: " + email);
        System.out.println("DEBUG: Subject: " + subject);

        javaMailSender.send(message);
        System.out.println("DEBUG: sendStatusChangeNotification - Email sent successfully to '" + recipientEmail + "' for status: " + newStatus.name());
    }

    @Async
    @Override
    public void sendPasswordResetEmail(String recipientEmail, String resetToken) throws MessagingException {
        System.out.println("DEBUG: sendPasswordResetEmail - Attempting to send password reset email to: '" + recipientEmail + "'");

        String resetLink = "http://localhost:5173/reset-password?token=" + resetToken;

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

    @Async
    @Override
    public void sendHtmlEmail(String recipientEmail, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(recipientEmail);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        helper.setFrom(email);

        System.out.println("DEBUG: sendHtmlEmail - Preparing to send email:");
        System.out.println("DEBUG: To: " + recipientEmail);
        System.out.println("DEBUG: From: " + email);
        System.out.println("DEBUG: Subject: " + subject);

        javaMailSender.send(message);
        System.out.println("DEBUG: sendHtmlEmail - Email sent successfully to '" + recipientEmail + "' with subject: " + subject);
    }


    // CHỈNH SỬA PHƯƠNG THỨC NÀY ĐỂ TRUYỀN THAM SỐ VÀO EMAIL TEMPLATE ĐÚNG CÁCH
//    @Async
//    @Override
//    public void sendTransportUnitApprovalNotification(String recipientEmail, String transportUnitName, String userName, ApprovalStatus status, String managerNote) throws MessagingException {
//        System.out.println("DEBUG: sendTransportUnitApprovalNotification - Attempting to send notification to: '" + recipientEmail + "' for Transport Unit '" + transportUnitName + "' with status: " + status.name() + " (User: " + userName + ")");
//
//        String subject;
//        String htmlBody;
//
//        // userName đã được truyền vào, sử dụng trực tiếp
//        String displayUserName = (userName != null && !userName.trim().isEmpty()) ? userName : "Bạn";
//
//        switch (status) {
//            case APPROVED:
//                subject = "[OperatorManagementSystem] Đơn vị Vận chuyển của bạn đã được duyệt";
//                // Gọi đúng phương thức static từ EmailTemplate
//                htmlBody = EmailTemplate.buildTransportUnitApprovedEmail(displayUserName, transportUnitName, managerNote);
//                break;
//            case REJECTED:
//                subject = "[OperatorManagementSystem] Đơn vị Vận chuyển của bạn đã bị từ chối";
//                // Gọi đúng phương thức static từ EmailTemplate
//                htmlBody = EmailTemplate.buildTransportUnitRejectedEmail(displayUserName, transportUnitName, managerNote);
//                break;
//            default:
//                subject = "[OperatorManagementSystem] Cập nhật trạng thái đơn vị Vận chuyển";
//                // Gọi đúng phương thức static từ EmailTemplate
//                htmlBody = EmailTemplate.buildGenericTransportUnitStatusUpdateEmail(displayUserName, transportUnitName, status.name(), managerNote);
//                break;
//        }
//
//        sendHtmlEmail(recipientEmail, subject, htmlBody);
//
//        System.out.println("DEBUG: sendTransportUnitApprovalNotification - Email sent successfully to '" + recipientEmail + "' for Transport Unit '" + transportUnitName + "' with status: " + status.name());
//    }
    @Async
    @Override
    public void sendTransportUnitApprovalNotification(String recipientEmail, String userName, String transportUnitName, ApprovalStatus status, String managerNote) throws MessagingException {
        System.out.println("DEBUG: sendTransportUnitApprovalNotification - Attempting to send notification to: '" + recipientEmail + "' for Transport Unit '" + transportUnitName + "' with status: " + status.name() + " (User: " + userName + ")");

        String subject;
        String htmlBody;

        // userName đã được truyền vào, sử dụng trực tiếp
        String displayUserName = (userName != null && !userName.trim().isEmpty()) ? userName : "Bạn";

        switch (status) {
            case APPROVED:
                subject = "[OperatorManagementSystem] Đơn vị Vận chuyển của bạn đã được duyệt";
                // Giữ nguyên logic này nếu bạn muốn sử dụng các static method build HTML
                // htmlBody = EmailTemplate.buildTransportUnitApprovedEmail(displayUserName, transportUnitName, managerNote);
                break;
            case REJECTED:
                subject = "[OperatorManagementSystem] Đơn vị Vận chuyển của bạn đã bị từ chối";
                // htmlBody = EmailTemplate.buildTransportUnitRejectedEmail(displayUserName, transportUnitName, managerNote);
                break;
            default:
                subject = "[OperatorManagementSystem] Cập nhật trạng thái đơn vị Vận chuyển";
                // htmlBody = EmailTemplate.buildGenericTransportUnitStatusUpdateEmail(displayUserName, transportUnitName, status.name(), managerNote);
                break;
        }

        // --- DÒNG THAY THẾ CHO MỤC ĐÍCH DEBUG VỚI PLAIN TEXT ---
        htmlBody = "Chào " + displayUserName + ",\n\n" +
                "Đơn vị vận chuyển " + transportUnitName + " của bạn đã được " + status.name() + ".\n" +
                "Ghi chú: " + (managerNote != null && !managerNote.isEmpty() ? managerNote : "Không có ghi chú.") + "\n\n" +
                "Trân trọng,\nHệ thống OperatorManagementSystem.";
        // --- KẾT THÚC DÒNG THAY THẾ ---

        sendHtmlEmail(recipientEmail, subject, htmlBody);

        System.out.println("DEBUG: sendTransportUnitApprovalNotification - Email sent successfully to '" + recipientEmail + "' for Transport Unit '" + transportUnitName + "' with status: " + status.name());
    }
}