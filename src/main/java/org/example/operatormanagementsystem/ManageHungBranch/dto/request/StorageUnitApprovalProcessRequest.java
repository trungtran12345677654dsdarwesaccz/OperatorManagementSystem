package org.example.operatormanagementsystem.ManageHungBranch.dto.request;

import lombok.*;
import org.example.operatormanagementsystem.enumeration.ApprovalStatus;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StorageUnitApprovalProcessRequest {
    private ApprovalStatus status;
    private String managerNote;
}
