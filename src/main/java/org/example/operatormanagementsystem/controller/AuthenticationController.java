package org.example.operatormanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.operatormanagementsystem.dto.request.LoginRequest;
import org.example.operatormanagementsystem.dto.response.AuthLoginResponse;
import org.example.operatormanagementsystem.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;



@RequestMapping("/api/auth")
@RestController
@AllArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    // Thay đổi kiểu trả về thành ResponseEntity<AuthLoginResponse>
    public ResponseEntity<AuthLoginResponse> login(@Valid @RequestBody LoginRequest request) {

        // Gọi service để thực hiện logic đăng nhập và nhận AuthLoginResponse
        AuthLoginResponse authLoginResponse = authenticationService.login(request);

        // Trả về ResponseEntity.ok() với AuthLoginResponse
        // Điều này sẽ trả về HTTP status 200 OK và AuthLoginResponse trong body
        return ResponseEntity.ok(authLoginResponse);
    }
    @GetMapping("/me")
    public ResponseEntity<String> getMe(Authentication authentication) {
        return ResponseEntity.ok("Hello " + authentication.getName());
    }
}
