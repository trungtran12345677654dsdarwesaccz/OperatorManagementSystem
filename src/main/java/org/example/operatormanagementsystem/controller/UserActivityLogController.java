package org.example.operatormanagementsystem.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.dto.response.UserActivityLogResponse;
import org.example.operatormanagementsystem.service.UserActivityLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/activity-log")
@RequiredArgsConstructor
public class UserActivityLogController {

    private final UserActivityLogService userActivityLogService;

    @GetMapping
    public ResponseEntity<List<UserActivityLogResponse>> getCurrentUserLogs(HttpServletRequest request) {
        return ResponseEntity.ok(userActivityLogService.getLogsForCurrentUser(request));
    }
}
