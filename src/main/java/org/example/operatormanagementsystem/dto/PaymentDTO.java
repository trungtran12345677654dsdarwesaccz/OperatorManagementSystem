package org.example.operatormanagementsystem.dto;

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
    private String payerType;
    private Integer payerId;
    private BigDecimal amount;
    private LocalDate paidDate;
    private String status;
    private String note;
}
