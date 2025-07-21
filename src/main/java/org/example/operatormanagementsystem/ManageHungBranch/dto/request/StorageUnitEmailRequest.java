package org.example.operatormanagementsystem.ManageHungBranch.dto.request;

import io.micrometer.observation.annotation.Observed;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StorageUnitEmailRequest {
    private String name;
    private String address;
    private Integer slotCount;
    private String phone;
    private String note;
    private String imageUrl;
    private String senderEmail;

}
