package org.example.operatormanagementsystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSessionResponse {
    private Long id;
    private String token;
    private String ipAddress;
    private String userAgent;
    private String deviceInfo;
    private LocalDateTime createdAt;
    private LocalDateTime lastAccessedAt;
    private boolean active;
    private String email;
    private String role;
}