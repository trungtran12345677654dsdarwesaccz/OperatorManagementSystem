package org.example.operatormanagementsystem.service.impl;

import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.example.operatormanagementsystem.config.JwtUtil;
import org.example.operatormanagementsystem.config.SecurityConfig;
import org.example.operatormanagementsystem.dto.request.LoginRequest;
import org.example.operatormanagementsystem.dto.request.RegisterRequest;
import org.example.operatormanagementsystem.dto.response.AuthLoginResponse;
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

@Service
@AllArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final SecurityConfig securityConfig;
    private final RoleRepository roleRepository;
    private final EmailService emailService;

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
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new BadCredentialsException("Your account is inactive. Please activate your account first.");
        }
        emailService.sendOTP(user.getEmail());

        // 4. Trả về một thông báo cho frontend biết OTP đã được gửi thành công.
        // Frontend sẽ chuyển sang màn hình nhập OTP.
        return "OTP has been sent to your email. Please enter it to complete login.";

    }

    @Override
    public UserResponse register(RegisterRequest register) {
        if (userRepository.existsByUsername(register.getUsername())) {
            throw new RuntimeException("Username already exists.");
        }

        if (userRepository.existsByEmail(register.getEmail())) {
            throw new RuntimeException("Email already exists.");
        }

        Users user = new Users();
        user.setUsername(register.getUsername());
        user.setEmail(register.getEmail());
        user.setPassword(securityConfig.passwordEncoder().encode(register.getPassword()));
        user.setPhone(register.getPhone());
        user.setFullName(register.getFullName());
        user.setAddress(register.getAddress());
        user.setGender(UserGender.valueOf(register.getGender().toUpperCase()));
        user.setStatus(UserStatus.ACTIVE); // hoặc ACTIVE nếu không xác thực
        user.setRole(UserRole.STAFF);

        userRepository.save(user);

        UserResponse response = new UserResponse();
        response.setUserName(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setGender(user.getGender().toString());
        response.setAddress(user.getAddress());
        return response;

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