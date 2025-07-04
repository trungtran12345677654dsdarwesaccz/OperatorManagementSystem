package org.example.operatormanagementsystem.customer_thai.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class    BookingCustomerRequest {
    private Integer storageId;
    private Integer transportId;
    private Integer operatorId;
    private LocalDateTime deliveryDate;
    private String note;
    private String imageStorageUnit;
    private String imageTransportUnit;
    private Long total;
} 