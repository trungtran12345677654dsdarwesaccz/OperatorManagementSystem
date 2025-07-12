package org.example.operatormanagementsystem.managestaff_yen.controller;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.managestaff_yen.dto.request.ChartFilterRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.request.TopOperatorFilterRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.response.*;
import org.example.operatormanagementsystem.managestaff_yen.service.DashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview")
    public DashboardOverviewResponse getOverview() {
        return dashboardService.getOverview();
    }

    @GetMapping("/recent-issues")
    public List<RecentIssueResponse> getRecentIssues(
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        if (fromDate == null) fromDate = LocalDate.now().minusMonths(1);
        if (toDate == null) toDate = LocalDate.now();

        return dashboardService.getRecentIssues(fromDate, toDate, limit);
    }



    @PostMapping("/top-operators")
    public List<TopOperatorResponse> getTopOperators(@RequestBody TopOperatorFilterRequest request) {
        return dashboardService.getTopOperators(request);
    }

    @PostMapping("/chart/orders")
    public List<ChartDataPointResponse> getOrderChart(@RequestBody ChartFilterRequest request) {
        return dashboardService.getOrderChartData(request);
    }

    @PostMapping("/chart/revenue")
    public List<ChartDataPointResponse> getRevenueChart(@RequestBody ChartFilterRequest request) {
        return dashboardService.getRevenueChartData(request);
    }

}