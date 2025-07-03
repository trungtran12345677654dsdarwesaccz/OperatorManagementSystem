package org.example.operatormanagementsystem.customer_thai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.operatormanagementsystem.enumeration.PaymentStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCustomerResponse {
    private Integer bookingId;
    private Integer customerId;
    private String customerName;
    private Integer storageId;
    private String storageName;
    private Integer transportId;
    private String transportName;
    private Integer operatorId;
    private String operatorName;
    private String pickupLocation;
    private String deliveryLocation;
    private String status;
    private PaymentStatus paymentStatus;
    private LocalDateTime createdAt;
    private LocalDateTime deliveryDate;
    private String note;
    private Long total;
    
    // Thông tin promotion
    private Long promotionId;
    private String promotionName;
    private String promotionDescription;
} 