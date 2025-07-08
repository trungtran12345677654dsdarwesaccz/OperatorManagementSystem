package org.example.operatormanagementsystem.customer_thai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotStatusResponse {
    private Integer storageId;
    private String storageName;
    private Integer totalSlots;
    private List<SlotInfo> slots;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SlotInfo {
        private Integer slotIndex;
        private boolean booked;
        private Integer bookingId; // null nếu chưa book
        private String customerName; // null nếu chưa book
    }
} 