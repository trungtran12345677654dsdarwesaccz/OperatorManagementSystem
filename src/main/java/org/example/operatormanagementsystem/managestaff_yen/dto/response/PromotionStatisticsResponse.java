package org.example.operatormanagementsystem.managestaff_yen.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionStatisticsResponse {
    private long totalPromotions;
    private long activePromotions;
    private long upcomingPromotions;
    private long expiredPromotions;
    private long canceledPromotions;
    private long pendingPromotions;
    private long totalPromotionBookings;
    private double totalPromotionRevenue;
    private long positiveFeedbackCount;
}