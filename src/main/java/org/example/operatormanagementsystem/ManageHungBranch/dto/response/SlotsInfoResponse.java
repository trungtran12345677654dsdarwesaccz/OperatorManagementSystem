package org.example.operatormanagementsystem.ManageHungBranch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SlotsInfoResponse {
    private Integer slotCount;
    private List<Integer> bookedSlots;
}
