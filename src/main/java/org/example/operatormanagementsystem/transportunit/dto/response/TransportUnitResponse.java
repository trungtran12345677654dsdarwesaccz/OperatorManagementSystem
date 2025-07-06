package org.example.operatormanagementsystem.transportunit.dto.response;

import lombok.Builder;
import lombok.Data;
import org.example.operatormanagementsystem.enumeration.TransportAvailabilityStatus;
import org.example.operatormanagementsystem.enumeration.UserStatus;

@Data
@Builder
public class TransportUnitResponse {
    private Integer transportId;
    private String nameCompany;
    private String namePersonContact;
    private String phone;
    private String licensePlate;
    private Integer numberOfVehicles;
    private Double capacityPerVehicle;
    private UserStatus status;
    private TransportAvailabilityStatus availabilityStatus;
    private String certificateFrontUrl;
    private String certificateBackUrl;

    private String note;
}
