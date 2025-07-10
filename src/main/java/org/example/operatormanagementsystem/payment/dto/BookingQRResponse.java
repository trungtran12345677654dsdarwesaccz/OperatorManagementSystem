package org.example.operatormanagementsystem.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookingQRResponse {
    private String qrUrl;
    private String note;
    private BigDecimal amount;
}
