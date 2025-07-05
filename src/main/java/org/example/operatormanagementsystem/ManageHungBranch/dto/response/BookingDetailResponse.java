package org.example.operatormanagementsystem.ManageHungBranch.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.operatormanagementsystem.managecustomerorderbystaff.dto.response.BookingResponse;

@Data
@EqualsAndHashCode(callSuper = true)
public class BookingDetailResponse extends BookingResponse {
    private Integer slotIndex;
}

