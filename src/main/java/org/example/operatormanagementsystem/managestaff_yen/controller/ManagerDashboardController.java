package org.example.operatormanagementsystem.managestaff_yen.controller;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.managestaff_yen.dto.response.ManagerDashboardResponse;
import org.example.operatormanagementsystem.managestaff_yen.service.ManagerDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/manager/{managerId}/dashboard")
@RequiredArgsConstructor
public class ManagerDashboardController {

    private final ManagerDashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ManagerDashboardResponse> getDashboard(@PathVariable Integer managerId) {
        return ResponseEntity.ok(dashboardService.getDashboardData(managerId));
    }
}
