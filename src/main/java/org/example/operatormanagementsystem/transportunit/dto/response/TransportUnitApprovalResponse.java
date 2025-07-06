package org.example.operatormanagementsystem.transportunit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.operatormanagementsystem.enumeration.ApprovalStatus;
import org.example.operatormanagementsystem.enumeration.TransportAvailabilityStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransportUnitApprovalResponse {
    private Integer approvalId;
    private Integer transportUnitId;
    private String transportUnitName;
    private Integer requestedByUserId;
    private String senderEmail;
    private Integer approvedByManagerId;
    private String approvedByManagerEmail;
    private ApprovalStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private String certificateFrontUrl;
    private String certificateBackUrl;
    private String managerNote;
    private Integer numberOfVehicles;
    private Double capacityPerVehicle;
    private TransportAvailabilityStatus availabilityStatus;
}
