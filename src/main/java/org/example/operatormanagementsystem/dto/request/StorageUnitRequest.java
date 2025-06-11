package org.example.operatormanagementsystem.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StorageUnitRequest {
    private String name;
    private String address;
    private Integer managerId;
    private String phone;
    private String status;
    private String note;
}
