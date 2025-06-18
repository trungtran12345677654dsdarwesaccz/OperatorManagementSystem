package org.example.operatormanagementsystem.managecustomerorderbystaff.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingDTO {
    private Integer bookingId;
    private Integer customerId;
    private Integer storageUnitId;
    private Integer transportUnitId;
    private Integer operatorId;
    private String status;
    private LocalDateTime deliveryDate;
    private String note;
}