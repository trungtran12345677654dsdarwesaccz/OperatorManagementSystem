package org.example.operatormanagementsystem.managePendingStaff.dto.request;

import lombok.*;
import org.example.operatormanagementsystem.enumeration.ApprovalStatus;
import org.example.operatormanagementsystem.enumeration.UserStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalHistoryFilterRequest {
    private String userEmail;
    private String approvedByEmail;
    private ApprovalStatus status;
    private UserStatus fromStatus;
    private UserStatus toStatus;
    private String approvedByIp;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
}
