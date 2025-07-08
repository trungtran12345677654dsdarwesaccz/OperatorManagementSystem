package org.example.operatormanagementsystem.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.config.JwtUtil;
import org.example.operatormanagementsystem.dto.response.UserSessionResponse;
import org.example.operatormanagementsystem.entity.LoginHistory;
import org.example.operatormanagementsystem.entity.UserSession;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.repository.UserRepository;
import org.example.operatormanagementsystem.repository.UserSessionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {
    private final UserSessionRepository userSessionRepository;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @GetMapping()
    @PreAuthorize("hasAnyRole('MANAGER', 'STAFF')")
    public ResponseEntity<List<UserSessionResponse>> getSessions(HttpServletRequest request) {
        String email = jwtUtil.extractUsername(jwtUtil.extractTokenFromRequest(request));
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<UserSession> sessions = userSessionRepository.findTop10ByUserOrderByLastAccessedAtDesc(user);

        List<UserSessionResponse> response = sessions.stream().map(session -> UserSessionResponse.builder()
                .id(session.getId())
                .token(session.getToken())
                .ipAddress(session.getIpAddress())
                .userAgent(session.getUserAgent())
                .deviceInfo(session.getDeviceInfo())
                .createdAt(session.getCreatedAt())
                .lastAccessedAt(session.getLastAccessedAt())
                .active(session.isActive())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build()).toList();

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{tokenId}")
    public ResponseEntity<String> revokeSession(@PathVariable String tokenId, HttpServletRequest request) {
        String token = jwtUtil.extractTokenFromRequest(request);
        String email = jwtUtil.extractUsername(token);
        Users user = userRepository.findByEmail(email).orElseThrow();

        UserSession session = userSessionRepository.findByUserAndActiveTrue(user).stream()
                .filter(s -> s.getToken().equals(tokenId)).findFirst()
                .orElseThrow(() -> new RuntimeException("Session not found"));
        session.setActive(false);
        userSessionRepository.save(session);

        return ResponseEntity.ok("Session revoked");
    }


}
