package org.example.operatormanagementsystem.managecustomerorderbystaff.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequest {
    @NotBlank(message = "Status cannot be blank")
    private String status;

    @NotNull(message = "Delivery date cannot be null")
    private LocalDateTime deliveryDate;

    private String note;

    @NotNull(message = "Customer ID cannot be null")
    private Integer customerId;

    private String customerFullName;

    @NotNull(message = "Storage Unit ID cannot be null")
    private Integer storageUnitId;

    @NotNull(message = "Transport Unit ID cannot be null")
    private Integer transportUnitId;

    @NotNull(message = "Operator Staff ID cannot be null")
    private Integer operatorStaffId;

    @PositiveOrZero(message = "Total must be zero or positive")
    private Long total;

    @NotBlank(message = "Payment status cannot be blank")
    private String paymentStatus;

    @NotNull(message = "Slot index cannot be null")
    @PositiveOrZero(message = "Slot index must be zero or positive")
    private Integer slotIndex;

    @NotBlank(message = "Delivery location cannot be blank")
    private String deliveryLocation;

    @NotBlank(message = "Pickup location cannot be blank")
    private String pickupLocation;


}