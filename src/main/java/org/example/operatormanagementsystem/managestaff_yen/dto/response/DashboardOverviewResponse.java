package org.example.operatormanagementsystem.managestaff_yen.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class DashboardOverviewResponse {
    private long totalOrders;
    private BigDecimal revenueToday;
    private long activePromotions;
    private double onTimeRate;
}
