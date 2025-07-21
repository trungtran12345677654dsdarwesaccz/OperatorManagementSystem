package org.example.operatormanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.operatormanagementsystem.enumeration.ApprovalStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class StorageUnitApproval {
    @Id
    @Column(name = "approval_id")
    private Integer approvalId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "approval_id")
    private StorageUnit storageUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_user_id", nullable = false)
    private Users requestedByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_manager_id")
    private Manager approvedByManager;

    @Column(name = "status", length = 30, nullable = false)
    @Enumerated(EnumType.STRING)
    private ApprovalStatus status;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "manager_note", length = 500)
    private String managerNote;

    @Column(name = "sender_email")
    private String senderEmail;

    @PrePersist
    protected void onCreate() {
        if (this.requestedAt == null) this.requestedAt = LocalDateTime.now();
        if (this.status == null) this.status = ApprovalStatus.PENDING;
    }
}
