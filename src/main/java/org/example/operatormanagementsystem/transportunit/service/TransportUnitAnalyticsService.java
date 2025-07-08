package org.example.operatormanagementsystem.transportunit.service;

import org.example.operatormanagementsystem.enumeration.UserStatus;
import org.example.operatormanagementsystem.transportunit.dto.response.*;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface TransportUnitAnalyticsService {
    DashboardStatsResponse getDashboardStats();
    List<HistoricalDataResponse> getHistoricalData(LocalDate start, LocalDate end, String groupBy);
    List<WeeklyActivityResponse> getWeeklyActivity();
    List<ManagerPerformanceResponse> getManagerPerformance(LocalDate start, LocalDate end);
    List<StatusDistributionResponse> getStatusDistribution();
    List<ApprovalTrendResponse> getApprovalTrends(int days);
    PerformanceMetricsResponse getPerformanceMetrics();


}