package org.example.operatormanagementsystem.transportunit.dto.response;

import lombok.Builder;
import lombok.Data;
import org.example.operatormanagementsystem.enumeration.UserStatus;

@Data
@Builder
public class TransportUnitResponse {
    private Integer transportId;
    private String nameCompany;
    private String namePersonContact;
    private String phone;
    private String licensePlate;
    private UserStatus status;
    private String note;
}