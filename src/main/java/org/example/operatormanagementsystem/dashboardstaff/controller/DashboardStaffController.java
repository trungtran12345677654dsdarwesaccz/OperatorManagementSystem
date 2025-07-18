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

import java.util.List;

@RestController
@RequestMapping("/api/dashboard/staff")
@RequiredArgsConstructor
public class DashboardStaffController {

    private final DashboardStaffService dashboardStaffService;
    private final UserRepository userRepository;
    private final PositionRepository positionRepository;

    @PostMapping("/positions")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER')")
    public ResponseEntity<String> addPosition(@Valid @RequestBody DashboardStaffRequest request) {
        try {
            dashboardStaffService.addPosition(request);
            return ResponseEntity.ok("Thêm hoặc cập nhật chức vụ thành công!");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi khi thêm/cập nhật chức vụ: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi server khi thêm/cập nhật chức vụ: " + e.getMessage());
        }
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<DashboardStaffResponse> getDashboardStats() {
        try {
            DashboardStaffResponse stats = dashboardStaffService.getDashboardStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new DashboardStaffResponse());
        }
    }

    @GetMapping("/activities")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<RecentActivityResponse>> getRecentActivities() {
        try {
            List<RecentActivityResponse> activities = dashboardStaffService.getRecentActivities();
            return activities.isEmpty()
                    ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                    : ResponseEntity.ok(activities);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    @GetMapping("/positions")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<Position>> getUserPositions() {
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    @GetMapping("/monthly-revenue")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<MonthlyRevenueResponse>> getMonthlyRevenue(
            @RequestParam(required = false, defaultValue = "Tất cả") String year,
            @RequestParam(required = false, defaultValue = "Tất cả") String unit) {
        try {
            List<MonthlyRevenueResponse> revenue = dashboardStaffService.getMonthlyRevenue(year, unit);
            return revenue.isEmpty()
                    ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                    : ResponseEntity.ok(revenue);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    @GetMapping("/performance-data")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<PerformanceDataResponse>> getPerformanceData(
            @RequestParam(required = false, defaultValue = "Tất cả") String year,
            @RequestParam(required = false, defaultValue = "Tất cả") String unit) {
        try {
            List<PerformanceDataResponse> performance = dashboardStaffService.getPerformanceData(year, unit);
            return performance.isEmpty()
                    ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                    : ResponseEntity.ok(performance);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    @GetMapping("/detail-data")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<DetailDataResponse>> getDetailData(
            @RequestParam(required = false, defaultValue = "Tất cả") String year,
            @RequestParam(required = false, defaultValue = "Tất cả") String unit) {
        try {
            List<DetailDataResponse> detail = dashboardStaffService.getDetailData(year, unit);
            return detail.isEmpty()
                    ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                    : ResponseEntity.ok(detail);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    @GetMapping("/transport-data")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<TransportDataResponse> getTransportData(
            @RequestParam(required = false, defaultValue = "Tất cả") String year,
            @RequestParam(required = false, defaultValue = "Tất cả") String unit) {
        try {
            TransportDataResponse transport = dashboardStaffService.getTransportData(year, unit);
            return transport == null
                    ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                    : ResponseEntity.ok(transport);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new TransportDataResponse());
        }
    }

    @GetMapping("/ranking-data")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<RankingDataResponse>> getRankingData(
            @RequestParam(required = false, defaultValue = "month") String period,
            @RequestParam(required = false, defaultValue = "revenue") String metric) {
        try {
            List<RankingDataResponse> ranking = dashboardStaffService.getRankingData(period, metric);
            return ranking.isEmpty()
                    ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                    : ResponseEntity.ok(ranking);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    @GetMapping("/team-ranking")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<TeamRankingResponse>> getTeamRanking(
            @RequestParam(required = false, defaultValue = "month") String period,
            @RequestParam(required = false, defaultValue = "revenue") String metric) {
        try {
            List<TeamRankingResponse> teamRanking = dashboardStaffService.getTeamRanking(period, metric);
            return teamRanking.isEmpty()
                    ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                    : ResponseEntity.ok(teamRanking);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    @GetMapping("/achievements")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<AchievementResponse>> getAchievements() {
        try {
            List<AchievementResponse> achievements = dashboardStaffService.getAchievements();
            return achievements.isEmpty()
                    ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                    : ResponseEntity.ok(achievements);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }
}