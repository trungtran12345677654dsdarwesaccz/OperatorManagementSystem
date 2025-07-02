package org.example.operatormanagementsystem.managestaff_yen.service;

import org.example.operatormanagementsystem.managestaff_yen.dto.response.ManagerDashboardResponse;

public interface ManagerDashboardService {
    ManagerDashboardResponse getDashboardData(Integer managerId);
}
