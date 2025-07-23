package org.example.operatormanagementsystem.ManageHungBranch.dto.response;

import lombok.*;
import org.example.operatormanagementsystem.enumeration.ApprovalStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageUnitApprovalResponse {
    private Integer approvalId;
    private Integer storageUnitId;
    private String storageUnitName;
    private Integer requestedByUserId;
    private String senderEmail;
    private Integer approvedByManagerId;
    private String approvedByManagerEmail;
    private ApprovalStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private String managerNote;

    private String address;
    private String phone;
    private Integer slotCount;
    private String imageUrl;
    private String emailError;
}
