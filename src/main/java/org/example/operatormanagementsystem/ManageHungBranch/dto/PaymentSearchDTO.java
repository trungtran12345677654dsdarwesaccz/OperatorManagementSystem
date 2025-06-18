package org.example.operatormanagementsystem.ManageHungBranch.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentSearchDTO {
    private String status;
    private String payerType;
    private Integer payerId;
    private LocalDate fromDate;
    private LocalDate toDate;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String customerName;
    private String bookingCode;
    private Boolean isOverdue;
}