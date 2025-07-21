package org.example.operatormanagementsystem.ManageHungBranch.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageUnitResponse {
    private Integer storageId;
    private String name;
    private String address;
    private Integer slotCount;
    private String phone;
    private String status;
    private String note;
    private String image;
}
