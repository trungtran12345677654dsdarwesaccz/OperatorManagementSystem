package org.example.operatormanagementsystem.controller;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.operatormanagementsystem.config.JwtUtil;
import org.example.operatormanagementsystem.dto.request.*;
import org.example.operatormanagementsystem.dto.response.AuthLoginResponse;
import org.example.operatormanagementsystem.dto.response.UserResponse;
import org.example.operatormanagementsystem.service.AuthenticationService;
import org.example.operatormanagementsystem.service.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;


@RequestMapping("/api/auth")
@RestController
@AllArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;

    // Đây là endpoint cho BƯỚC 1: Xác thực password và GỬI OTP
    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request) { //
        try {
            // authenticationService.login(request) bây giờ trả về String
            String message = authenticationService.login(request); // <-- Dòng này bây giờ đã đúng kiểu

            // Trả về HTTP status 200 OK và thông báo String
            return ResponseEntity.ok(message);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send OTP: " + e.getMessage());
        }
    }

    // Bạn vẫn cần endpoint riêng cho BƯỚC 2: Xác minh OTP và nhận token
    // (như đã thảo luận ở các câu trả lời trước đó)
    @PostMapping("/login/verify-otp")
    public ResponseEntity<?> completeLoginWithOtp(@Valid @RequestBody VerifyOTPRequest request,
                                                  HttpServletRequest servletRequest) {
        try {
            // Gọi phương thức verifyOtp mới đã được sửa đổi trong EmailServiceImpl (Canvas)
            request.setIp(servletRequest.getRemoteAddr());
            request.setUserAgent(servletRequest.getHeader("User-Agent"));
            request.setDeviceInfo(parseDeviceInfo(request.getUserAgent()));
            AuthLoginResponse authLoginResponse = emailService.verifyOtp(request);
            return ResponseEntity.ok(authLoginResponse);

        } catch (BadCredentialsException e) {
            // Xử lý các lỗi BadCredentialsException (OTP không hợp lệ/hết hạn/không tìm thấy, hoặc tài khoản inactive)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (UsernameNotFoundException e) {
            // Xử lý lỗi khi không tìm thấy người dùng sau xác minh OTP
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // Hoặc HttpStatus.UNAUTHORIZED
        } catch (AuthenticationException e) {
            // Xử lý các loại AuthenticationException khác
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            // Xử lý các lỗi không mong muốn khác
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred during OTP verification: " + e.getMessage());
        }
    }

    @PostMapping("/sendOTP")
    public ResponseEntity<String> sendOTP(@Valid @RequestBody SendOTPRequest request) { // Đã thay đổi kiểu trả về
        try {
            emailService.sendOTP(request.getEmail());

            return ResponseEntity.ok("OTP has been sent to your email.");
        } catch (MessagingException e) {
            // Xử lý lỗi khi gửi email
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send OTP email: " + e.getMessage());
        } catch (Exception e) {
            // Xử lý các lỗi không mong muốn khác
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse userResponse = authenticationService.register(request);
        return ResponseEntity.ok(userResponse);
    }

    // Endpoint Yêu cầu thay đổi trạng thái (VD: từ INACTIVE -> PENDING_APPROVAL)
    @PostMapping("/request-status-change")
    public ResponseEntity<String> requestStatusChange(@Valid @RequestBody StatusChangeRequest request) {
        try {
            String message = authenticationService.requestStatusChange(request);
            return ResponseEntity.ok(message);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to submit status change request: " + e.getMessage());
        }
    }



    @PostMapping("/forgot-password")
    public ResponseEntity<String> requestPasswordReset(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            authenticationService.requestPasswordReset(request.getEmail());
            // Luôn trả về thông báo chung chung để tránh tiết lộ email tồn tại hay không
            return ResponseEntity.ok("If an account with that email exists, a password reset link has been sent.");
        } catch (UsernameNotFoundException e) {
            // Vẫn trả về thông báo chung ngay cả khi không tìm thấy người dùng
            return ResponseEntity.ok("If an account with that email exists, a password reset link has been sent.");
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send password reset email: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred during password reset request: " + e.getMessage());
        }
    }

    // --- NEW ENDPOINT: Đặt lại mật khẩu ---
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            authenticationService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok("Password has been reset successfully.");
        } catch (BadCredentialsException e) { // Dùng BadCredentialsException cho token không hợp lệ/hết hạn
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (UsernameNotFoundException e) { // Người dùng không tìm thấy dù token hợp lệ (ít xảy ra)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) { // Mật khẩu mới không hợp lệ (ví dụ: quá ngắn, quá dài)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred during password reset: " + e.getMessage());
        }
    }
    @PostMapping("/change-password-request")
    @PreAuthorize("hasAnyRole('MANAGER', 'STAFF')")
    public ResponseEntity<String> requestChangePassword(HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is missing.");
            }
            String email = jwtUtil.extractUsername(token);
            authenticationService.requestPasswordReset(email); // Gửi email như quên mật khẩu
            return ResponseEntity.ok("A confirmation link has been sent to your email.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send email: " + e.getMessage());
        }
    }



    @PostMapping("/logout")
    public ResponseEntity<String> logout(Authentication authentication) {
        try {
            if (authentication != null) {

            }
            return ResponseEntity.ok("Đăng xuất thành công.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi đăng xuất: " + e.getMessage());
        }
    }
    private String parseDeviceInfo(String userAgent) {
        if (userAgent == null) return "Unknown";

        String agent = userAgent.toLowerCase();
        if (agent.contains("windows")) return "Windows";
        if (agent.contains("mac")) return "Mac";
        if (agent.contains("android")) return "Android";
        if (agent.contains("iphone")) return "iPhone";
        if (agent.contains("linux")) return "Linux";

        return "Other";
    }

}
