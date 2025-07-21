package org.example.operatormanagementsystem.schedulemanagement.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.operatormanagementsystem.schedulemanagement.service.ScheduleNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for schedule notification operations
 * Provides endpoints for notification badge functionality
 * Requirements: 5.4, 5.5
 */
@RestController
@RequestMapping("/api/schedule/notifications")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ScheduleNotificationController {
    
    private final ScheduleNotificationService scheduleNotificationService;
    
    /**
     * Get count of unread schedule notifications for an operator
     * Requirements: 5.5
     */
    @GetMapping("/unread-count/{operatorId}")
    public ResponseEntity<?> getUnreadNotificationCount(@PathVariable Integer operatorId) {
        try {
            log.debug("Getting unread notification count for operator: {}", operatorId);
            
            int unreadCount = scheduleNotificationService.getUnreadScheduleNotificationCount(operatorId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", unreadCount,
                "operatorId", operatorId
            ));
            
        } catch (Exception e) {
            log.error("Error getting unread notification count for operator {}: {}", operatorId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Không thể lấy số lượng thông báo chưa đọc",
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Mark all schedule notifications as read for an operator
     * Requirements: 5.4
     */
    @PatchMapping("/mark-read/{operatorId}")
    public ResponseEntity<?> markNotificationsAsRead(@PathVariable Integer operatorId) {
        try {
            log.debug("Marking schedule notifications as read for operator: {}", operatorId);
            
            scheduleNotificationService.markScheduleNotificationsAsRead(operatorId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã đánh dấu tất cả thông báo lịch làm việc là đã đọc",
                "operatorId", operatorId
            ));
            
        } catch (Exception e) {
            log.error("Error marking notifications as read for operator {}: {}", operatorId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Không thể đánh dấu thông báo là đã đọc",
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Health check endpoint for schedule notifications
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Schedule notification service is running",
            "timestamp", System.currentTimeMillis()
        ));
    }
}