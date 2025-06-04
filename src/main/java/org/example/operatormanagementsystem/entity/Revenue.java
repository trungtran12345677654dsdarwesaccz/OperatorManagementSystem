package org.example.operatormanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "revenue")
@ToString(of = {"revenueId", "beneficiaryType", "amount", "date"})
public class Revenue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "revenue_id")
    private Integer revenueId;

    @Column(name = "beneficiary_type", length = 30)
    private String beneficiaryType;

    @Column(name = "beneficiary_id")
    private Integer beneficiaryId;

    @Column(name = "source_type", length = 30)
    private String sourceType;

    @Column(name = "source_id")
    private Integer sourceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Column(name = "amount", precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "date") // SQL type is 'date'
    private LocalDate date;

    @Column(name = "description", length = 255)
    private String description;
}