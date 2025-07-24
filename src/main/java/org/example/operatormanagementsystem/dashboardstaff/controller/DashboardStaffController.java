package org.example.operatormanagementsystem.dashboardstaff.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.dashboardstaff.dto.request.DashboardStaffRequest;
import org.example.operatormanagementsystem.dashboardstaff.dto.response.DashboardStaffResponse;
import org.example.operatormanagementsystem.dashboardstaff.dto.response.MonthlyRevenueResponse;
import org.example.operatormanagementsystem.dashboardstaff.dto.response.PerformanceDataResponse;
import org.example.operatormanagementsystem.dashboardstaff.dto.response.DetailDataResponse;
import org.example.operatormanagementsystem.dashboardstaff.dto.response.TransportDataResponse;
import org.example.operatormanagementsystem.dashboardstaff.dto.response.RankingDataResponse;
import org.example.operatormanagementsystem.dashboardstaff.dto.response.RecentActivityResponse;
import org.example.operatormanagementsystem.dashboardstaff.dto.response.TeamRankingResponse;
import org.example.operatormanagementsystem.dashboardstaff.dto.response.AchievementResponse;
import org.example.operatormanagementsystem.dashboardstaff.service.DashboardStaffService;
import org.example.operatormanagementsystem.entity.Position;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.repository.UserRepository;
import org.example.operatormanagementsystem.dashboardstaff.repository.PositionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard/staff")
@RequiredArgsConstructor
public class DashboardStaffController {

    private final DashboardStaffService dashboardStaffService;
    private final UserRepository userRepository;
    private final PositionRepository positionRepository;

    // Helper method to create consistent error response
    private ResponseEntity<Map<String, String>> createErrorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(Collections.singletonMap("error", message));
    }

    @PostMapping("/positions")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER')")
    public ResponseEntity<Map<String, String>> addPosition(@Valid @RequestBody DashboardStaffRequest request) {
        try {
            dashboardStaffService.addPosition(request);
            return ResponseEntity.ok(Collections.singletonMap("message", "Thêm hoặc cập nhật chức vụ thành công!"));
        } catch (RuntimeException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.BAD_REQUEST, "Lỗi khi thêm/cập nhật chức vụ: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi server khi thêm/cập nhật chức vụ: " + e.getMessage());
        }
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> getDashboardStats() {
        try {
            DashboardStaffResponse stats = dashboardStaffService.getDashboardStats();
            return ResponseEntity.ok(stats != null ? stats : new DashboardStaffResponse());
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi khi lấy thống kê: " + e.getMessage());
        }
    }

    @GetMapping("/activities")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> getRecentActivities() {
        try {
            List<RecentActivityResponse> activities = dashboardStaffService.getRecentActivities();
            return activities.isEmpty()
                    ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                    : ResponseEntity.ok(activities);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi khi lấy hoạt động gần đây: " + e.getMessage());
        }
    }

    @GetMapping("/positions")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> getUserPositions() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = principal instanceof UserDetails ? ((UserDetails) principal).getUsername() : principal.toString();
            Users user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + username));
            List<Position> positions = positionRepository.findByUserId(user.getId());
            return positions.isEmpty()
                    ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                    : ResponseEntity.ok(positions);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi khi lấy danh sách chức vụ: " + e.getMessage());
        }
    }

    @GetMapping("/monthly-revenue")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> getMonthlyRevenue(
            @RequestParam(defaultValue = "Tất cả") String year,
            @RequestParam(defaultValue = "Tất cả") String unit,
            @RequestParam(defaultValue = "1") String startMonth,
            @RequestParam(defaultValue = "12") String endMonth) {
        try {
            // Validate input parameters
            int startMonthInt = Integer.parseInt(startMonth);
            int endMonthInt = Integer.parseInt(endMonth);
            if (startMonthInt < 1 || startMonthInt > 12 || endMonthInt < 1 || endMonthInt > 12 || startMonthInt > endMonthInt) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Tháng không hợp lệ hoặc tháng bắt đầu lớn hơn tháng kết thúc");
            }
            if (!year.equals("Tất cả")) {
                try {
                    int yearInt = Integer.parseInt(year);
                    if (yearInt < 2000 || yearInt > 2025) {
                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Năm không hợp lệ (phải từ 2000 đến 2025)");
                    }
                } catch (NumberFormatException e) {
                    return createErrorResponse(HttpStatus.BAD_REQUEST, "Năm không hợp lệ");
                }
            }

            List<MonthlyRevenueResponse> revenue = dashboardStaffService.getMonthlyRevenue(year, unit, startMonth, endMonth);
            return revenue.isEmpty()
                    ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                    : ResponseEntity.ok(revenue);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi khi lấy dữ liệu doanh thu: " + e.getMessage());
        }
    }

    @GetMapping("/performance-data")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> getPerformanceData(
            @RequestParam(defaultValue = "Tất cả") String year,
            @RequestParam(defaultValue = "Tất cả") String unit,
            @RequestParam(defaultValue = "1") String startMonth,
            @RequestParam(defaultValue = "12") String endMonth) {
        try {
            // Validate input parameters
            int startMonthInt = Integer.parseInt(startMonth);
            int endMonthInt = Integer.parseInt(endMonth);
            if (startMonthInt < 1 || startMonthInt > 12 || endMonthInt < 1 || endMonthInt > 12 || startMonthInt > endMonthInt) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Tháng không hợp lệ hoặc tháng bắt đầu lớn hơn tháng kết thúc");
            }
            if (!year.equals("Tất cả")) {
                try {
                    int yearInt = Integer.parseInt(year);
                    if (yearInt < 2000 || yearInt > 2025) {
                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Năm không hợp lệ (phải từ 2000 đến 2025)");
                    }
                } catch (NumberFormatException e) {
                    return createErrorResponse(HttpStatus.BAD_REQUEST, "Năm không hợp lệ");
                }
            }

            List<PerformanceDataResponse> performance = dashboardStaffService.getPerformanceData(year, unit, startMonth, endMonth);
            return performance.isEmpty()
                    ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                    : ResponseEntity.ok(performance);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi khi lấy dữ liệu hiệu suất: " + e.getMessage());
        }
    }

    @GetMapping("/detail-data")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> getDetailData(
            @RequestParam(defaultValue = "Tất cả") String year,
            @RequestParam(defaultValue = "Tất cả") String unit,
            @RequestParam(defaultValue = "1") String startMonth,
            @RequestParam(defaultValue = "12") String endMonth) {
        try {
            // Validate input parameters
            int startMonthInt = Integer.parseInt(startMonth);
            int endMonthInt = Integer.parseInt(endMonth);
            if (startMonthInt < 1 || startMonthInt > 12 || endMonthInt < 1 || endMonthInt > 12 || startMonthInt > endMonthInt) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Tháng không hợp lệ hoặc tháng bắt đầu lớn hơn tháng kết thúc");
            }
            if (!year.equals("Tất cả")) {
                try {
                    int yearInt = Integer.parseInt(year);
                    if (yearInt < 2000 || yearInt > 2025) {
                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Năm không hợp lệ (phải từ 2000 đến 2025)");
                    }
                } catch (NumberFormatException e) {
                    return createErrorResponse(HttpStatus.BAD_REQUEST, "Năm không hợp lệ");
                }
            }

            List<DetailDataResponse> detail = dashboardStaffService.getDetailData(year, unit, startMonth, endMonth);
            return detail.isEmpty()
                    ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                    : ResponseEntity.ok(detail);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi khi lấy dữ liệu chi tiết: " + e.getMessage());
        }
    }

    @GetMapping("/transport-data")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> getTransportData(
            @RequestParam(defaultValue = "Tất cả") String year,
            @RequestParam(defaultValue = "Tất cả") String unit,
            @RequestParam(defaultValue = "1") String startMonth,
            @RequestParam(defaultValue = "12") String endMonth) {
        try {
            // Validate input parameters
            int startMonthInt = Integer.parseInt(startMonth);
            int endMonthInt = Integer.parseInt(endMonth);
            if (startMonthInt < 1 || startMonthInt > 12 || endMonthInt < 1 || endMonthInt > 12 || startMonthInt > endMonthInt) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Tháng không hợp lệ hoặc tháng bắt đầu lớn hơn tháng kết thúc");
            }
            if (!year.equals("Tất cả")) {
                try {
                    int yearInt = Integer.parseInt(year);
                    if (yearInt < 2000 || yearInt > 2025) {
                        return createErrorResponse(HttpStatus.BAD_REQUEST, "Năm không hợp lệ (phải từ 2000 đến 2025)");
                    }
                } catch (NumberFormatException e) {
                    return createErrorResponse(HttpStatus.BAD_REQUEST, "Năm không hợp lệ");
                }
            }

            TransportDataResponse transport = dashboardStaffService.getTransportData(year, unit, startMonth, endMonth);
            return transport == null
                    ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                    : ResponseEntity.ok(transport);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi khi lấy dữ liệu vận chuyển: " + e.getMessage());
        }
    }

    @GetMapping("/ranking-data")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> getRankingData(
            @RequestParam(defaultValue = "month") String period,
            @RequestParam(defaultValue = "revenue") String metric) {
        try {
            // Validate period and metric
            if (!List.of("week", "month", "quarter", "year", "all").contains(period.toLowerCase())) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Khoảng thời gian không hợp lệ");
            }
            if (!List.of("revenue", "trips", "success_rate").contains(metric.toLowerCase())) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Chỉ số không hợp lệ");
            }

            List<RankingDataResponse> ranking = dashboardStaffService.getRankingData(period, metric);
            return ranking.isEmpty()
                    ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                    : ResponseEntity.ok(ranking);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi khi lấy dữ liệu xếp hạng: " + e.getMessage());
        }
    }

    @GetMapping("/team-ranking")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> getTeamRanking(
            @RequestParam(defaultValue = "month") String period,
            @RequestParam(defaultValue = "revenue") String metric) {
        try {
            // Validate period and metric
            if (!List.of("week", "month", "quarter", "year", "all").contains(period.toLowerCase())) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Khoảng thời gian không hợp lệ");
            }
            if (!List.of("revenue", "trips", "success_rate").contains(metric.toLowerCase())) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Chỉ số không hợp lệ");
            }

            List<TeamRankingResponse> teamRanking = dashboardStaffService.getTeamRanking(period, metric);
            return teamRanking.isEmpty()
                    ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                    : ResponseEntity.ok(teamRanking);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi khi lấy dữ liệu xếp hạng đội nhóm: " + e.getMessage());
        }
    }

    @GetMapping("/achievements")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> getAchievements() {
        try {
            List<AchievementResponse> achievements = dashboardStaffService.getAchievements();
            return achievements.isEmpty()
                    ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                    : ResponseEntity.ok(achievements);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi khi lấy dữ liệu thành tích: " + e.getMessage());
        }
    }
}