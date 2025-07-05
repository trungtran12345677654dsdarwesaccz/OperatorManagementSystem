package org.example.operatormanagementsystem.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.config.JwtUtil;
import org.example.operatormanagementsystem.dto.response.UserUsageStatDto;
import org.example.operatormanagementsystem.entity.UserUsageStat;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.repository.UserRepository;
import org.example.operatormanagementsystem.repository.UserUsageStatRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/usage")
public class UsageStatController {

    private final UserUsageStatRepository usageStatRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<UserUsageStatDto> getUsageStat(HttpServletRequest request) {
        String email = jwtUtil.extractUsername(jwtUtil.resolveToken(request));
        Users user = userRepository.findByEmail(email).orElseThrow();
        UserUsageStat stat = usageStatRepository.findByUser(user).orElseThrow();
        return ResponseEntity.ok(convertToDto(stat));
    }

    private UserUsageStatDto convertToDto(UserUsageStat stat) {
        UserUsageStatDto dto = new UserUsageStatDto();
        dto.setId(stat.getId());
        dto.setApiCallsToday(stat.getApiCallsToday());
        dto.setCurrentDate(stat.getCurrentDate());
        dto.setLastLoginAt(stat.getLastLoginAt());
        dto.setLoginCount(stat.getLoginCount());
        dto.setTotalOnlineSeconds(stat.getTotalOnlineSeconds());
        dto.setUserId(stat.getId());
        return dto;
    }
}
