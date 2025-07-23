package org.example.operatormanagementsystem.customer_thai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.operatormanagementsystem.enumeration.DiscountType;
import org.example.operatormanagementsystem.enumeration.PromotionStatus;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionBookingResponse {
    private Long id;
    private String name;
    private String description;
    private Date startDate;
    private Date endDate;
    private PromotionStatus status;
    private DiscountType discountType;
    private Double discountValue;
} 