package org.example.operatormanagementsystem.dashboardstaff.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DashboardStaffResponse {
    private int newReceipts;
    private int pendingOrders;
    private int newCustomers;
    private int pendingSupport;
    private int pendingCustomers; // Khách Hàng Chờ
}