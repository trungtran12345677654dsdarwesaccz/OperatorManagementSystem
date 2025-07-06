package org.example.operatormanagementsystem.transportunit.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.example.operatormanagementsystem.enumeration.UserStatus;
import org.example.operatormanagementsystem.enumeration.TransportAvailabilityStatus;

@Data
public class TransportUnitRequest {

    @NotBlank(message = "Company name cannot be blank")
    @Size(max = 100, message = "Company name cannot exceed 100 characters")
    private String nameCompany;

    @NotBlank(message = "Contact person name cannot be blank")
    @Size(max = 100, message = "Contact person name cannot exceed 100 characters")
    private String namePersonContact;

    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(
            regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$",
            message = "Invalid phone number format"
    )
    private String phone;

    @NotBlank(message = "License plate cannot be blank")
    @Size(max = 20, message = "License plate cannot exceed 20 characters")
    private String licensePlate;

    @Min(value = 1, message = "Number of vehicles must be at least 1")
    @Max(value = 999, message = "Number of vehicles cannot exceed 999")
    private Integer numberOfVehicles;

    @DecimalMin(value = "0.1", inclusive = true, message = "Capacity per vehicle must be at least 0.1 m³")
    @DecimalMax(value = "100.0", inclusive = true, message = "Capacity per vehicle cannot exceed 100.0 m³")
    @Digits(integer = 3, fraction = 2, message = "Capacity must be a decimal with up to 2 digits after the decimal point")
    private Double capacityPerVehicle;

    private TransportAvailabilityStatus availabilityStatus;

    private String certificateFrontUrl;

    private String certificateBackUrl;


    private UserStatus status;

    @Size(max = 255, message = "Note cannot exceed 255 characters")
    private String note;
}
