package org.example.operatormanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.operatormanagementsystem.enumeration.TimeOffStatus;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "time_off_request")
public class TimeOffRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Integer requestId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private Users operator;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TimeOffStatus status = TimeOffStatus.PENDING;
    
    @Column(name = "manager_comments", columnDefinition = "TEXT")
    private String managerComments;
    
    @CreationTimestamp
    @Column(name = "request_date", updatable = false)
    private LocalDateTime requestDate;
    
    @Column(name = "reviewed_date")
    private LocalDateTime reviewedDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private Manager reviewedBy;
    
    @PrePersist
    protected void onCreate() {
        if (this.requestDate == null) {
            this.requestDate = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        if (this.status != TimeOffStatus.PENDING && this.reviewedDate == null) {
            this.reviewedDate = LocalDateTime.now();
        }
    }
}