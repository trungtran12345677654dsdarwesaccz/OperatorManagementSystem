package org.example.operatormanagementsystem.managecustomerorderbystaff.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingResponse {
    private Integer bookingId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime deliveryDate;
    private String note;
    private Integer customerId;
    private String customerFullName;
    private Long total; // Thêm tổng giá trị đơn hàng (số nguyên VNĐ)
    private String paymentStatus;
    private Integer slotIndex;

}