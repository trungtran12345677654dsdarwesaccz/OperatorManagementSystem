package org.example.operatormanagementsystem.controller;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.operatormanagementsystem.dto.request.LoginRequest;
import org.example.operatormanagementsystem.dto.request.RegisterRequest;
import org.example.operatormanagementsystem.dto.request.SendOTPRequest;
import org.example.operatormanagementsystem.dto.request.VerifyOTPRequest;
import org.example.operatormanagementsystem.dto.response.AuthLoginResponse;
import org.example.operatormanagementsystem.dto.response.UserResponse;
import org.example.operatormanagementsystem.service.AuthenticationService;
import org.example.operatormanagementsystem.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @RestController
    @RequestMapping("/api/auth")
    public class AuthController {

        @Autowired
        private AuthenticationService authenticationService;

        // Đây là endpoint cho BƯỚC 1: Xác thực password và GỬI OTP
        @PostMapping("/login")
        public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request) { // <-- Thay đổi AuthLoginResponse thành String ở đây
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
        public ResponseEntity<?> completeLoginWithOtp(@Valid @RequestBody VerifyOTPRequest request) {
            try {
                // Gọi phương thức verifyOtp mới đã được sửa đổi trong EmailServiceImpl (Canvas)
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

                // Trả về chuỗi thông báo thành công
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

    }
}
