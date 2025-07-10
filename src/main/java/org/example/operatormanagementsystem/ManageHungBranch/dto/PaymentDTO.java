package org.example.operatormanagementsystem.ManageHungBranch.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {
    private Integer paymentId;
    private Integer bookingId;
    private String bookingCode;

    private Integer payerUserId;
    private String payerFullName;

    private BigDecimal amount;
    private LocalDate paidDate;
    private String status;
    private String note;
    private String transactionNo;

    private Boolean isOverdue;
    private Integer daysPastDue;
}
