package org.example.operatormanagementsystem.service;

import jakarta.servlet.http.HttpServletRequest;
import org.example.operatormanagementsystem.dto.response.UserActivityLogResponse;
import org.example.operatormanagementsystem.entity.Users;

import java.util.List;

public interface UserActivityLogService {
    void log(Users user, String action, String metadata);
    List<UserActivityLogResponse> getLogsForCurrentUser(HttpServletRequest request);
}
