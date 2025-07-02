package org.example.operatormanagementsystem.managestaff_yen.dto.response;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StaffPerformanceOverviewResponse {

    private long totalStaffs;
    private long activeStaffs;
    private long inactiveStaffs;
    private long blockedStaffs;

    private Map<String, Long> monthlyCreatedStats; // "07/2025" → 12 nhân viên
    private Map<String, Long> statusDistribution;  // "ACTIVE" → 15

    private List<OperatorPerformanceDTO> topBookingStaffs;
    private List<OperatorPerformanceDTO> topFeedbackStaffs;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OperatorPerformanceDTO {
        private Integer operatorId;
        private String fullName;
        private long totalBookings;
        private long totalFeedbacks;
    }
}
