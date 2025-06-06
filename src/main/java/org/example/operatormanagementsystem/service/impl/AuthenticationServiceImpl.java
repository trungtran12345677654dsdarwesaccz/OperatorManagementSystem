package org.example.operatormanagementsystem.service.impl;

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
import org.example.operatormanagementsystem.dto.request.repository.RoleRepository;
import org.example.operatormanagementsystem.dto.request.repository.UserRepository;
import org.example.operatormanagementsystem.service.AuthenticationService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final SecurityConfig securityConfig;
    private final RoleRepository roleRepository;
    @Override
    public AuthLoginResponse login(LoginRequest request) {
        // Xác thực thông tin đăng nhập
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Lấy thông tin user sau khi xác thực thành công
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Tạo và trả về JWT token (sử dụng email là username)
        String token = jwtUtil.generateToken(userDetails);  // userDetails.getUsername() là email
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(Object::toString)
                .orElse("USER");

        AuthLoginResponse authLoginResponse = new AuthLoginResponse();
        authLoginResponse.setAccessToken(token);
        authLoginResponse.setRole(role);
        return authLoginResponse;
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
