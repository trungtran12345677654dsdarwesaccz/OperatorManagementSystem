package org.example.operatormanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import org.example.operatormanagementsystem.enumeration.ApprovalStatus;
import org.example.operatormanagementsystem.enumeration.UserStatus;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserApprovalHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Người bị duyệt
    @ManyToOne
    private Users user;

    // Người duyệt
    @ManyToOne
    private Users approvedBy;

    @Enumerated(EnumType.STRING)
    private ApprovalStatus status;

    private String note;

    private LocalDateTime approvedAt;

    @Enumerated(EnumType.STRING)
    private UserStatus fromStatus;

    @Enumerated(EnumType.STRING)
    private UserStatus toStatus;

    private String approvedByIp;
}

