package org.example.operatormanagementsystem.transportunit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private int totalUnits;              // COUNT(*) FROM TransportUnit
    private int pendingApprovals;        // WHERE status = PENDING_APPROVAL
    private int activeUnits;             // WHERE status = ACTIVE
    private int inactiveUnits;           // WHERE status = INACTIVE
    private double approvalRate;         // approved / total (from TransportUnitApproval)
    private double avgProcessingTime;    // AVG(processedAt - requestedAt)
    private int todayApprovals;          // approvals today
    private int todayRejections;         // rejections today
}

