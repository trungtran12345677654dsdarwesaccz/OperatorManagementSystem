package org.example.operatormanagementsystem.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleCalendarResponse {
    private LocalDate date;
    private List<OrderSummary> orders;
    private List<ShiftInfo> shifts;
    private TimeOffStatus timeOffStatus;
    private int totalOrders;
    private String workStatus; // WORKING, TIME_OFF, AVAILABLE
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderSummary {
        private Integer bookingId;
        private String customerName;
        private String pickupLocation;
        private String deliveryLocation;
        private String status;
        private String deliveryTime;
        private Long total;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ShiftInfo {
        private Integer shiftId;
        private String shiftName;
        private String startTime;
        private String endTime;
        private String status;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimeOffStatus {
        private boolean hasTimeOff;
        private String reason;
        private String status;
    }
}