package org.example.operatormanagementsystem.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorageUnitSummaryDTO {
    private Integer storageId;
    private String name;
    private String address;
    private String status;
    private Integer availableCapacity;
    private Integer totalBookings;
}
