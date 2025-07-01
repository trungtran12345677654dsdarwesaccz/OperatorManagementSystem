package org.example.operatormanagementsystem.transportunit.dto.request;

import lombok.Data;
import org.example.operatormanagementsystem.enumeration.UserStatus;

import java.time.LocalDateTime;

@Data
public class TransportUnitSearchRequest {
    private String keyword;                 // nameCompany, namePersonContact, phone, licensePlate
    private Integer transportId;            // exact match
    private UserStatus status;              // status of transport unit

    // From Booking
    private Integer bookingId;
}
