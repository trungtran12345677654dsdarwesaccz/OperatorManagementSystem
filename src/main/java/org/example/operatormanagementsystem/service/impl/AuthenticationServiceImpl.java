package org.example.operatormanagementsystem.service.impl;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.operatormanagementsystem.config.JwtUtil;
import org.example.operatormanagementsystem.config.SecurityConfig;
import org.example.operatormanagementsystem.dto.request.LoginRequest;
import org.example.operatormanagementsystem.dto.request.RegisterRequest;
import org.example.operatormanagementsystem.dto.request.StatusChangeRequest;
import org.example.operatormanagementsystem.dto.response.UserResponse;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.enumeration.UserGender;
import org.example.operatormanagementsystem.enumeration.UserRole;
import org.example.operatormanagementsystem.enumeration.UserStatus;
import org.example.operatormanagementsystem.repository.RoleRepository;
import org.example.operatormanagementsystem.repository.UserRepository;
import org.example.operatormanagementsystem.service.AuthenticationService;
import org.example.operatormanagementsystem.service.EmailService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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
        throw new RuntimeException("    Email already exists.");
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
    user.getCreatedDate();


    userRepository.save(user);

    // --- Bắt đầu phần thay đổi: Tự động gửi yêu cầu thay đổi trạng thái sau khi đăng ký ---
    // Tạo một StatusChangeRequest giả định cho mục đích này
    StatusChangeRequest statusChangeRequest = StatusChangeRequest.builder()
            .email(user.getEmail()) // Sử dụng email của user vừa tạo
            .build();
    // Gọi phương thức requestStatusChange của chính service này
    // Lưu ý: Kết quả của requestStatusChange (là String) sẽ được gắn vào errorMessage của UserResponse
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

    // Phương thức chung để quản lý thay đổi trạng thái bởi MANAGER
    @Override
    @Transactional
    public Users updateStatusByManager(String email, UserStatus newStatus) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        UserStatus oldStatus = user.getStatus(); // Lưu trạng thái cũ để kiểm tra logic gửi email

        switch (newStatus) {
            case ACTIVE:
                if (oldStatus == UserStatus.PENDING_APPROVAL || oldStatus == UserStatus.INACTIVE) {
                    user.setStatus(UserStatus.ACTIVE);
                    userRepository.save(user);
                    try {
                        emailService.sendStatusChangeNotification(user.getEmail(), UserStatus.ACTIVE); // Gửi email kích hoạt
                    } catch (MessagingException e) {
                        System.err.println("Failed to send activation email to " + user.getEmail() + ": " + e.getMessage());
                        // Có thể log lỗi chi tiết hơn hoặc ném lại ngoại lệ nếu việc gửi email là quan trọng
                    }
                    return user;
                } else if (oldStatus == UserStatus.ACTIVE) {
                    throw new IllegalStateException("User with email: " + email + " is already active.");
                } else {
                    throw new IllegalStateException("Cannot activate user from current status: " + oldStatus);
                }
            case REJECTED:
                // Nếu người dùng đang PENDING_APPROVAL và bị REJECT, chuyển về INACTIVE
                if (oldStatus == UserStatus.PENDING_APPROVAL) {
                    user.setStatus(UserStatus.INACTIVE); // Chuyển về INACTIVE khi bị từ chối
                    userRepository.save(user);
                    try {
                        // Gửi email thông báo trạng thái INACTIVE (do bị từ chối)
                        emailService.sendStatusChangeNotification(user.getEmail(), UserStatus.INACTIVE);
                    } catch (MessagingException e) {
                        System.err.println("Failed to send rejection (to inactive) email to " + user.getEmail() + ": " + e.getMessage());
                    }
                    return user;
                }
                // Các trường hợp khác khi cố gắng REJECT:
                else if (oldStatus == UserStatus.INACTIVE) {
                    throw new IllegalStateException("User with email: " + email + " is already INACTIVE. Cannot change to REJECTED.");
                } else if (oldStatus == UserStatus.ACTIVE) {
                    throw new IllegalStateException("Cannot reject an ACTIVE user directly. Please set to INACTIVE first if necessary.");
                } else if (oldStatus == UserStatus.REJECTED) {
                    throw new IllegalStateException("User with email: " + email + " is already rejected.");
                } else {
                    throw new IllegalStateException("Cannot reject user from current status: " + oldStatus);
                }
            case INACTIVE:
                if (oldStatus == UserStatus.ACTIVE) {
                    user.setStatus(UserStatus.INACTIVE);
                    userRepository.save(user);
                    try {
                        emailService.sendStatusChangeNotification(user.getEmail(), UserStatus.INACTIVE); // Gửi email vô hiệu hóa
                    } catch (MessagingException e) {
                        System.err.println("Failed to send inactive email to " + user.getEmail() + ": " + e.getMessage());
                    }
                    return user;
                } else if (oldStatus == UserStatus.INACTIVE) {
                    throw new IllegalStateException("User with email: " + email + " is already inactive.");
                } else {
                    throw new IllegalStateException("Cannot set user to inactive from current status: " + oldStatus);
                }
            default:
                throw new IllegalArgumentException("Invalid status for manager update: " + newStatus);
        }
    }
    @Override
    public UserResponse getUserDetailsForManager(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        UserResponse response = new UserResponse();
        response.setUserName(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setGender(user.getGender().toString());
        response.setAddress(user.getAddress());
        response.setStatus(user.getStatus());
        return response;
    }

    @Override
    public List<UserResponse> getUsersByStatus(UserStatus status) {
        List<Users> users = userRepository.findByStatus(UserStatus.PENDING_APPROVAL);
        // Trả về một danh sách rỗng nếu không có người dùng nào khớp
        return users.stream()
                .map(user -> {
                    UserResponse response = new UserResponse();
                    response.setUserName(user.getUsername());
                    response.setEmail(user.getEmail());
                    response.setFullName(user.getFullName());
                    response.setGender(user.getGender().toString());
                    response.setAddress(user.getAddress());
                    response.setStatus(user.getStatus());
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> getUsersNeedingManagerAction() {
        List<Users> pendingUsers = userRepository.findByStatus(UserStatus.PENDING_APPROVAL);
        List<Users> inactiveUsers = userRepository.findByStatus(UserStatus.INACTIVE);

        List<Users> usersToReview = new ArrayList<>(pendingUsers);
        usersToReview.addAll(inactiveUsers);

        // Trả về một danh sách rỗng nếu không có người dùng nào cần duyệt
        return usersToReview.stream()
                .map(user -> {
                    UserResponse response = new UserResponse();
                    response.setUserName(user.getUsername());
                    response.setEmail(user.getEmail());
                    response.setFullName(user.getFullName());
                    response.setGender(user.getGender().toString());
                    response.setAddress(user.getAddress());
                    response.setStatus(user.getStatus());
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void requestPasswordReset(String email) throws MessagingException {
        // Tìm người dùng theo email
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Tạo token đặt lại mật khẩu
        String resetToken = jwtUtil.generatePasswordResetToken(user);

        // Gửi email chứa liên kết đặt lại mật khẩu
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
        System.out.println("Password reset link sent to: " + email);
    }

    // --- NEW METHOD: Đặt lại mật khẩu ---
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
/*
    String token = jwtUtil.generateToken(userDetails);
    String role = userDetails.getAuthorities().stream()
            .findFirst()
            .map(Object::toString)
            .orElse("USER");

    AuthLoginResponse authLoginResponse = new AuthLoginResponse();
    authLoginResponse.setAccessToken(token);
    authLoginResponse.setRole(role);
    return authLoginResponse;
    */