package org.example.operatormanagementsystem.customer_thai.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateBookingRequest {
    @NotNull(message = "Storage ID is required")
    private Integer storageId;

    @NotNull(message = "Transport ID is required")
    private Integer transportId;

    @NotNull(message = "Operator ID is required")
    private Integer operatorId;

    @NotBlank(message = "Pickup location is required")
    private String pickupLocation;

    @NotBlank(message = "Delivery location is required")
    private String deliveryLocation;

    @Future(message = "Delivery date must be in the future")
    private LocalDateTime deliveryDate;
    
    private String note;

    @NotNull(message = "Total is required")
    private Long total;
} 