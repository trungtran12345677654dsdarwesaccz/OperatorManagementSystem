package org.example.operatormanagementsystem.dashboardstaff.service;

import org.example.operatormanagementsystem.dashboardstaff.dto.request.DashboardStaffRequest;
import org.example.operatormanagementsystem.dashboardstaff.dto.response.DashboardStaffResponse;
import org.example.operatormanagementsystem.dashboardstaff.dto.response.RecentActivityResponse;

import java.util.List;

public interface DashboardStaffService {
    void addPosition(DashboardStaffRequest request);
    DashboardStaffResponse getDashboardStats();
    List<RecentActivityResponse> getRecentActivities();
}