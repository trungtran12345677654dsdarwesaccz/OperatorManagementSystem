package org.example.operatormanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "payment")
@ToString(of = {"paymentId", "amount", "status"})
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Integer paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Column(name = "payer_type", length = 50)
    private String payerType;

    @Column(name = "payer_id")
    private Integer payerId;

    @Column(precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Column(length = 50)
    private String status;

    @Column(length = 255)
    private String note;
}