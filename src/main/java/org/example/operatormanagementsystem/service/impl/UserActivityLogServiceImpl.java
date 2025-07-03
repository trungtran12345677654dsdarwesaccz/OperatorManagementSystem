package org.example.operatormanagementsystem.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.config.JwtUtil;
import org.example.operatormanagementsystem.dto.response.UserActivityLogResponse;
import org.example.operatormanagementsystem.entity.UserActivityLog;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.repository.UserActivityLogRepository;
import org.example.operatormanagementsystem.repository.UserRepository;
import org.example.operatormanagementsystem.service.UserActivityLogService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserActivityLogServiceImpl implements UserActivityLogService {

    private final UserActivityLogRepository activityLogRepository;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    public void log(Users user, String action, String metadata) {
        UserActivityLog log = new UserActivityLog();
        log.setUser(user);
        log.setAction(action);
        log.setMetadata(metadata);
        log.setTimestamp(LocalDateTime.now());
        activityLogRepository.save(log);
    }

    @Override
    public List<UserActivityLogResponse> getLogsForCurrentUser(HttpServletRequest request) {
        String token = jwtUtil.resolveToken(request);
        String email = jwtUtil.extractUsername(token);
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));

        return activityLogRepository.findByUserOrderByTimestampDesc(user).stream().map(log -> {
            UserActivityLogResponse dto = new UserActivityLogResponse();
            dto.setAction(log.getAction());
            dto.setMetadata(log.getMetadata());
            dto.setTimestamp(log.getTimestamp());
            return dto;
        }).collect(Collectors.toList());
    }
}
