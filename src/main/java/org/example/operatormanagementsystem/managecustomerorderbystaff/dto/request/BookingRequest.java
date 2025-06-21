package org.example.operatormanagementsystem.managecustomerorderbystaff.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "Storage Unit ID cannot be null")
    private Integer storageUnitId;

    @NotNull(message = "Transport Unit ID cannot be null")
    private Integer transportUnitId;

    @NotNull(message = "Operator Staff ID cannot be null")
    private Integer operatorStaffId;
}