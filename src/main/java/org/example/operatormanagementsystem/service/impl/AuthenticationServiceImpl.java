package org.example.operatormanagementsystem.service.impl;

import org.example.operatormanagementsystem.dto.request.LoginRequest;
import org.example.operatormanagementsystem.dto.response.AuthLoginResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public class AuthenticationServiceImpl {
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(List.of(authProvider));
    }
    public AuthLoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager().authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        AuthResponseLogin authResponseLogin = new AuthResponseLogin();
        authResponseLogin.setAccessToken(jwtUtil.generateToken(userDetails));
        return authResponseLogin;
    }
}
