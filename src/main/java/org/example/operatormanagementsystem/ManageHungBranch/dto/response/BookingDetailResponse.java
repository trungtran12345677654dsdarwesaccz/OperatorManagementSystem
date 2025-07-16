package org.example.operatormanagementsystem.ManageHungBranch.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.operatormanagementsystem.managecustomerorderbystaff.dto.response.BookingResponse;

@Data
@EqualsAndHashCode(callSuper = true)
public class BookingDetailResponse extends BookingResponse {
    private Integer slotIndex;
    private String pickupLocation;
    private String deliveryLocation;
    private String pickupLocationName;
    private String deliveryLocationName;
    private Integer operatorStaffId;
    private String operatorStaffName;      // Tên nhân viên vận hành
    private Integer transportUnitId;
    private String transportUnitName;      // Tên đơn vị vận chuyển
}

