package org.example.operatormanagementsystem.transportunit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.operatormanagementsystem.enumeration.ApprovalStatus;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransportUnitApprovalResponse {
    private Integer approvalId;
    private Integer transportUnitId;
    private Integer requestedByUserId;
    private String senderEmail;
    private Integer approvedByManagerId;
    private ApprovalStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private String managerNote;

}
