package org.example.operatormanagementsystem.customer_thai.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.config.JwtUtil;
import org.example.operatormanagementsystem.customer_thai.dto.request.LoginRequest;
import org.example.operatormanagementsystem.customer_thai.dto.response.LoginResponse;
import org.example.operatormanagementsystem.customer_thai.service.CustomerAuthService;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.enumeration.UserRole;
import org.example.operatormanagementsystem.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service("customerAuthServiceImpl_thai")
@RequiredArgsConstructor
public class CustomerAuthServiceImpl implements CustomerAuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponse login(LoginRequest request) {
        try {
            Users user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found after authentication"));

            if (user.getRole() != UserRole.CUSTOMER) {
                throw new RuntimeException("Access denied. Only users with CUSTOMER role can log in here.");
            }

            if (user.getCustomer() == null) {
                throw new RuntimeException("User is not properly linked to a customer profile.");
            }


//            if (user.getManager() == null) {
//                throw new RuntimeException("User is not linked to any manager (required by JWT)");
//            }

            String token = jwtUtil.generateToken(user);

            return LoginResponse.builder()
                    .accessToken(token)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .role(user.getRole())
                    .build();

        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid email or password");
        }
    }
}