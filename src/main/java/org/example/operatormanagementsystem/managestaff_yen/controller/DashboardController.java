package org.example.operatormanagementsystem.managestaff_yen.controller;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.managestaff_yen.dto.request.ChartFilterRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.request.TopOperatorFilterRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.response.*;
import org.example.operatormanagementsystem.managestaff_yen.service.DashboardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:5174")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview")
    public DashboardOverviewResponse getOverview() {
        return dashboardService.getOverview();
    }

    @GetMapping("/recent-issues")
    public List<RecentIssueResponse> getRecentIssues(@RequestParam(defaultValue = "5") int limit) {
        return dashboardService.getRecentIssues(limit);
    }

    @PostMapping("/top-operators")
    public List<TopOperatorResponse> getTopOperators(@RequestBody TopOperatorFilterRequest request) {
        return dashboardService.getTopOperators(request);
    }

    @PostMapping("/chart")
    public List<ChartDataPointResponse> getChart(@RequestBody ChartFilterRequest request) {
        return dashboardService.getChartData(request);
    }
}
