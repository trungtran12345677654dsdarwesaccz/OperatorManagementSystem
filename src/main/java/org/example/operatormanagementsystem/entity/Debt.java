package org.example.operatormanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "debt")
@ToString(of = {"debtId", "amount", "status"})
public class Debt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "debt_id")
    private Integer debtId;

    @Column(name = "debtor_type", length = 30)
    private String debtorType;

    @Column(name = "debtor_id")
    private Integer debtorId;

    @Column(name = "creditor_type", length = 30)
    private String creditorType;

    @Column(name = "creditor_id")
    private Integer creditorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Column(precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(length = 255)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(length = 50)
    private String status;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}