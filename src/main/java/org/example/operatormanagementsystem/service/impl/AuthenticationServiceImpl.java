package org.example.operatormanagementsystem.service.impl;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.operatormanagementsystem.config.JwtUtil;
import org.example.operatormanagementsystem.config.SecurityConfig;
import org.example.operatormanagementsystem.dto.request.LoginRequest;
import org.example.operatormanagementsystem.dto.request.RegisterRequest;
import org.example.operatormanagementsystem.dto.request.StatusChangeRequest;
import org.example.operatormanagementsystem.dto.response.UserResponse;
import org.example.operatormanagementsystem.dto.response.UserSessionResponse;
import org.example.operatormanagementsystem.entity.LoginHistory;
import org.example.operatormanagementsystem.entity.UserSession;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.enumeration.UserGender;
import org.example.operatormanagementsystem.enumeration.UserRole;
import org.example.operatormanagementsystem.enumeration.UserStatus;
import org.example.operatormanagementsystem.repository.LoginHistoryRepository;
import org.example.operatormanagementsystem.repository.RoleRepository;
import org.example.operatormanagementsystem.repository.UserRepository;
import org.example.operatormanagementsystem.repository.UserSessionRepository;
import org.example.operatormanagementsystem.service.AuthenticationService;
import org.example.operatormanagementsystem.service.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final SecurityConfig securityConfig;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final LoginHistoryRepository loginHistoryRepository;
    private final UserSessionRepository userSessionRepository;

    @Override
    public List<LoginHistory> getLoginHistory(HttpServletRequest request) {
        String token = jwtUtil.resolveToken(request);
        String email = jwtUtil.extractUsername(token);
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));
        return loginHistoryRepository.findByUserOrderByLoginTimeDesc(user);
    }

    @Override
    public List<UserSessionResponse> getActiveSessions(HttpServletRequest request) {
        String token = jwtUtil.resolveToken(request);
        String email = jwtUtil.extractUsername(token);

        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));

        List<UserSession> sessions = userSessionRepository.findByUserAndActiveTrue(user);

        return sessions.stream().map(session -> {
            UserSessionResponse dto = new UserSessionResponse();
            dto.setId(session.getId());
            dto.setIpAddress(session.getIpAddress());
            dto.setDeviceInfo(session.getDeviceInfo());
            dto.setUserAgent(session.getUserAgent());
            dto.setCreatedAt(session.getCreatedAt());
            dto.setLastAccessedAt(session.getLastAccessedAt());
            dto.setEmail(user.getEmail());
            dto.setRole(user.getRole().name());
            return dto;
        }).collect(Collectors.toList());
    }



    @Override
// 2. Thêm throws MessagingException vào chữ ký của phương thức login
    public String login(LoginRequest request) throws MessagingException {
        // Xác thực thông tin đăng nhập
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Lấy thông tin user sau khi xác thực thành công
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        Users user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found after authentication."));

        // Kiểm tra trạng thái tài khoản. Nếu INACTIVE thì không cho tiếp tục
        if (user.getStatus() == UserStatus.INACTIVE || user.getStatus() == UserStatus.PENDING_APPROVAL|| user.getStatus() == UserStatus.REJECTED) {
            throw new BadCredentialsException("Your account is inactive. Please activate your account first.");
        }
        emailService.sendOTP(user.getEmail());

        // 4. Trả về một thông báo cho frontend biết OTP đã được gửi thành công.
        // Frontend sẽ chuyển sang màn hình nhập OTP.
        return "OTP has been sent to your email. Please enter it to complete login.";

    }
//securityConfig.passwordEncoder().encode(register.getPassword())
@Override
@Transactional
public UserResponse register(RegisterRequest register) {
    if (userRepository.existsByUsername(register.getUsername())) {
        throw new RuntimeException("Username already exists.");
    }

    if (userRepository.existsByEmail(register.getEmail())) {
        throw new RuntimeException("   Email already exists.");
    }
    if (register.getPassword().length() > 72) {
        throw new IllegalArgumentException("Password cannot be more than 72 characters.");
    }

    Users user = new Users();
    user.setUsername(register.getUsername());
    user.setEmail(register.getEmail());
    user.setPassword(securityConfig.passwordEncoder().encode(register.getPassword()));
    user.setPhone(register.getPhone());
    user.setFullName(register.getFullName());
    user.setAddress(register.getAddress());
    user.setGender(UserGender.valueOf(register.getGender().toUpperCase()));
    user.setStatus(UserStatus.PENDING_APPROVAL); // Mặc định là pending
    user.setRole(UserRole.STAFF); // Giữ nguyên vai trò bạn đã thiết lập
    user.getCreatedAt();


    userRepository.save(user);

    // --- Bắt đầu phần thay đổi: Tự động gửi yêu cầu thay đổi trạng thái sau khi đăng ký ---
    // Tạo một StatusChangeRequest giả định cho mục đích này
    StatusChangeRequest statusChangeRequest = StatusChangeRequest.builder()
            .email(user.getEmail()) // Sử dụng email của user vừa tạo
            .build();
    String statusChangeMessage = this.requestStatusChange(statusChangeRequest); // Sử dụng 'this' để gọi phương thức của cùng class
    System.out.println("DEBUG: Auto status change request initiated for new user: " + statusChangeMessage);
    // --- Kết thúc phần thay đổi ---

    UserResponse response = new UserResponse();
    response.setUserName(user.getUsername());
    response.setEmail(user.getEmail());
    response.setFullName(user.getFullName());
    response.setGender(user.getGender().toString());
    response.setAddress(user.getAddress());
    return response;
}

    // Phương thức yêu cầu thay đổi trạng thái (VD: từ INACTIVE -> PENDING_APPROVAL)
    @Override
    @Transactional
    public String requestStatusChange(StatusChangeRequest request) {
        Users user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + request.getEmail()));

        if (user.getStatus() == UserStatus.PENDING_APPROVAL) {
            return "Your account is already pending approval. Please wait for a manager to approve it.";
        }
        if (user.getStatus() == UserStatus.ACTIVE) {
            return "Your account is already active. No status change request needed.";
        }

        // Nếu user đang ở trạng thái INACTIVE, chuyển sang PENDING_APPROVAL
        if (user.getStatus() == UserStatus.INACTIVE || user.getStatus() == UserStatus.REJECTED) { // Có thể yêu cầu duyệt lại từ REJECTED
            user.setStatus(UserStatus.PENDING_APPROVAL);
            userRepository.save(user);
            return "Status change request submitted successfully. Your account is now pending manager approval.";
        } else {
            return "Cannot change status from current state: " + user.getStatus();
        }
    }


    @Override
    @Transactional
    public void requestPasswordReset(String email) throws MessagingException {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        String resetToken = jwtUtil.generatePasswordResetToken(user);
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
        System.out.println("Password reset link sent to: " + email);
    }
    // ---  Đặt lại mật khẩu ---
    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        // Trích xuất email từ token
        String email = jwtUtil.extractUsername(token);
        if (email == null) {
            throw new BadCredentialsException("Invalid or malformed reset password token.");
        }

        // Tìm người dùng theo email
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found for password reset."));

        // Xác minh token
        if (!jwtUtil.validateResetPasswordToken(token, user)) {
            throw new BadCredentialsException("Invalid or expired password reset token.");
        }

        // Kiểm tra độ dài mật khẩu mới
        if (newPassword == null || newPassword.length() < 6 || newPassword.length() > 72) {
            throw new IllegalArgumentException("New password must be between 6 and 72 characters.");
        }

        // Mã hóa và cập nhật mật khẩu mới
        user.setPassword(securityConfig.passwordEncoder().encode(newPassword));
        // Cập nhật lastPasswordResetDate để vô hiệu hóa tất cả các token reset cũ hơn
        user.setLastPasswordResetDate(LocalDateTime.now());
        userRepository.save(user);

        System.out.println("Password for user " + email + " has been reset successfully.");
    }


}
