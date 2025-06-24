package org.example.operatormanagementsystem.transportunit.controller;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.transportunit.dto.response.*;
import org.example.operatormanagementsystem.transportunit.service.TransportUnitAnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transport-unit-analytics")
@RequiredArgsConstructor
public class TransportUnitAnalyticsController {

    private final TransportUnitAnalyticsService analyticsService;

    @GetMapping("/dashboard-stats")
    @PreAuthorize("hasAnyRole('MANAGER')")
    public ResponseEntity<?> getDashboardStats() {
        try {
            DashboardStatsResponse stats = analyticsService.getDashboardStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Không thể lấy dữ liệu thống kê tổng quan.");
        }
    }

    @GetMapping("/historical-data")
    @PreAuthorize("hasAnyRole('MANAGER')")
    public ResponseEntity<?> getHistoricalData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "MONTH") String groupBy) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().body("Ngày bắt đầu và kết thúc không hợp lệ.");
        }
        try {
            List<HistoricalDataResponse> data = analyticsService.getHistoricalData(startDate, endDate, groupBy);
            return ResponseEntity.ok(data);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi khi lấy dữ liệu lịch sử.");
        }
    }

    @GetMapping("/weekly-activity")
    @PreAuthorize("hasAnyRole('MANAGER')")
    public ResponseEntity<?> getWeeklyActivity() {
        try {
            return ResponseEntity.ok(analyticsService.getWeeklyActivity());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi khi lấy dữ liệu hoạt động theo tuần.");
        }
    }

    @GetMapping("/manager-performance")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> getManagerPerformance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            return ResponseEntity.ok(analyticsService.getManagerPerformance(startDate, endDate));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi khi lấy hiệu suất quản lý.");
        }
    }

    @GetMapping("/status-distribution")
    @PreAuthorize("hasAnyRole('MANAGER')")
    public ResponseEntity<?> getStatusDistribution() {
        try {
            return ResponseEntity.ok(analyticsService.getStatusDistribution());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi khi thống kê trạng thái.");
        }
    }

    @GetMapping("/approval-trends")
    @PreAuthorize("hasAnyRole('MANAGER')")
    public ResponseEntity<?> getApprovalTrends(
            @RequestParam(defaultValue = "30") int days) {
        try {
            return ResponseEntity.ok(analyticsService.getApprovalTrends(days));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi khi lấy dữ liệu xu hướng duyệt.");
        }
    }

    @GetMapping("/performance-metrics")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> getPerformanceMetrics() {
        try {
            return ResponseEntity.ok(analyticsService.getPerformanceMetrics());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi khi phân tích hiệu suất hệ thống.");
        }
    }
}
