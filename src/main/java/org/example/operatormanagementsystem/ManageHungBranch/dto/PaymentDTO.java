    package org.example.operatormanagementsystem.ManageHungBranch.dto;

    import lombok.*;

    import java.math.BigDecimal;
    import java.time.LocalDate;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public class PaymentDTO {
        private Integer paymentId;
        private Integer bookingId;
        private String bookingCode; // Thêm để hiển thị mã booking
        private String customerName; // Thêm để hiển thị tên khách hàng
        private String payerType;
        private Integer payerId;
        private BigDecimal amount;
        private LocalDate paidDate;
        private String status;
        private String note;

        // Thêm các field để staff dễ theo dõi
        private Boolean isOverdue;
        private Integer daysPastDue;
        private String paymentMethod;
    }