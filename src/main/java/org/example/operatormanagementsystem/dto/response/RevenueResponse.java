package org.example.operatormanagementsystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevenueResponse {
    private Integer revenueId;
    private String beneficiaryType;
    private Integer beneficiaryId;
    private String sourceType;
    private Integer sourceId;
    private BigDecimal amount;
    private LocalDate date;
    private String description;
    private BookingBasicInfo booking;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BookingBasicInfo {
        private Integer bookingId;
        private CustomerBasicInfo customer;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CustomerBasicInfo {
        private Integer customerId;
        private UserBasicInfo users;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserBasicInfo {
        private Integer id;
        private String fullName;
        private String email;
    }
}
