package org.example.operatormanagementsystem.managestaff_yen.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ManagerDashboardResponse {
    private long totalOrdersToday;
    private long successfulOrders;
    private long ongoingOrders;
    private long failedOrders;
    private long onlineOperators;
    private long totalRevenueToday;
    private List<MonthlyPerformance> monthlyPerformance;
    private List<TopOperator> topOperators;
    private List<IssueOrderInfo> recentIssues;
    private List<OrderStatusDistribution> orderStatusDistribution; // Thêm vào đây

    @Data
    @Builder
    public static class MonthlyPerformance {
        private String monthLabel;
        private long successfulOrders;
        private long failedOrders;
    }

    @Data
    @Builder
    public static class TopOperator {
        private Integer operatorId;
        private String fullName;
        private long totalOrders;
        private double averageRating;
    }

    @Data
    @Builder
    public static class IssueOrderInfo {
        private Integer orderId;
        private String customerName;
        private String status;
        private String note;
        private String createdAt;
    }

    // Đảm bảo lớp này là public để có thể truy cập từ bên ngoài package
    @Data
    @Builder
    public static class OrderStatusDistribution {
        private String status;
        private long value;
    }
}