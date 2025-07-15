package org.example.operatormanagementsystem.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private String id;
    private String userId;
    private String fullName;
    private String paymentMethod;
    private Double amount;
    private String status;
    private String description;
}