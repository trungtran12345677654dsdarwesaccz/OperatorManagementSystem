package org.example.operatormanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.operatormanagementsystem.enumeration.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Integer paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_user_id", nullable = false)
    private Users payer;


    @Column(precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "paid_date")
    private LocalDate paidDate;


    @Column(name = "transaction_no", length = 100)
    private String transactionNo;

}