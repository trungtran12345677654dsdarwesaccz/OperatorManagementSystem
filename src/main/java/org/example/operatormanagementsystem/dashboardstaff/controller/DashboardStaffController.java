package org.example.operatormanagementsystem.dashboardstaff.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.example.operatormanagementsystem.dashboardstaff.dto.request.DashboardStaffRequest;
import org.example.operatormanagementsystem.dashboardstaff.dto.response.DashboardStaffResponse;
import org.example.operatormanagementsystem.dashboardstaff.dto.response.RecentActivityResponse;
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
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/dashboard/staff")
@CrossOrigin(
        origins = {"http://localhost:5173", "http://localhost:3000"},
        allowCredentials = "true",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.OPTIONS}
)
public class DashboardStaffController {

    private static final Logger logger = Logger.getLogger(DashboardStaffController.class.getName());
    private final DashboardStaffService dashboardStaffService;
    private final UserRepository userRepository;
    private final PositionRepository positionRepository;

    public DashboardStaffController(DashboardStaffService dashboardStaffService,
                                    UserRepository userRepository,
                                    PositionRepository positionRepository) {
        this.dashboardStaffService = dashboardStaffService;
        this.userRepository = userRepository;
        this.positionRepository = positionRepository;
    }

    @PostMapping("/positions")
    @PreAuthorize("hasAnyRole('ROLE_STAFF', 'ROLE_ADMIN')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thêm hoặc cập nhật chức vụ thành công"),
            @ApiResponse(responseCode = "400", description = "Lỗi khi thêm/cập nhật chức vụ, ví dụ: trạng thái không hợp lệ hoặc người dùng không tồn tại"),
            @ApiResponse(responseCode = "401", description = "Không được phép truy cập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền thực hiện hành động"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public ResponseEntity<String> addPosition(@Valid @RequestBody DashboardStaffRequest request) {
        try {
            logger.info("Adding/updating position: " + request.getTitle() + " for user ID: " + request.getUserId());
            dashboardStaffService.addPosition(request);
            return ResponseEntity.ok("Thêm hoặc cập nhật chức vụ thành công!");
        } catch (RuntimeException e) {
            logger.severe("Error adding/updating position: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi khi thêm/cập nhật chức vụ: " + e.getMessage());
        }
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ROLE_STAFF')")
    public ResponseEntity<DashboardStaffResponse> getDashboardStats() {
        try {
            logger.info("Fetching dashboard stats for user: " + SecurityContextHolder.getContext().getAuthentication().getName());
            DashboardStaffResponse stats = dashboardStaffService.getDashboardStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.severe("Error fetching stats: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new DashboardStaffResponse());
        }
    }

    @GetMapping("/activities")
    @PreAuthorize("hasRole('ROLE_STAFF')")
    public ResponseEntity<List<RecentActivityResponse>> getRecentActivities() {
        try {
            logger.info("Fetching recent activities");
            List<RecentActivityResponse> activities = dashboardStaffService.getRecentActivities();
            return activities.isEmpty()
                    ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                    : ResponseEntity.ok(activities);
        } catch (Exception e) {
            logger.severe("Error fetching activities: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    @GetMapping("/positions")
    @PreAuthorize("hasRole('ROLE_STAFF')")
    public ResponseEntity<List<Position>> getUserPositions() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = principal instanceof UserDetails ? ((UserDetails) principal).getUsername() : principal.toString();
            logger.info("Fetching positions for user: " + username);

            Users user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + username));

            List<Position> positions = positionRepository.findByUserId(user.getId());
            return positions.isEmpty()
                    ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                    : ResponseEntity.ok(positions);
        } catch (Exception e) {
            logger.severe("Error fetching user positions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }
}