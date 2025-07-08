package org.example.operatormanagementsystem.managePendingStaff.dto.response;

import lombok.*;
import org.example.operatormanagementsystem.enumeration.ApprovalStatus;
import org.example.operatormanagementsystem.enumeration.UserStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserApprovalHistoryResponse {
    private String userEmail;
    private String approvedByEmail;
    private ApprovalStatus status;
    private String note;
    private LocalDateTime approvedAt;
    private UserStatus fromStatus;
    private UserStatus toStatus;
    private String approvedByIp;

}
