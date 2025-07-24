package org.example.operatormanagementsystem.dashboardstaff.service;

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

import java.util.List;

public interface DashboardStaffService {
    void addPosition(DashboardStaffRequest request);
    DashboardStaffResponse getDashboardStats();
    List<RecentActivityResponse> getRecentActivities();
    List<MonthlyRevenueResponse> getMonthlyRevenue(String year, String unit, String startMonth, String endMonth);
    List<PerformanceDataResponse> getPerformanceData(String year, String unit, String startMonth, String endMonth);
    List<DetailDataResponse> getDetailData(String year, String unit, String startMonth, String endMonth);
    TransportDataResponse getTransportData(String year, String unit, String startMonth, String endMonth);
    List<RankingDataResponse> getRankingData(String period, String metric);
    List<TeamRankingResponse> getTeamRanking(String period, String metric);
    List<AchievementResponse> getAchievements();
}