package org.example.operatormanagementsystem.transportunit.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TransportUnitEmailRequest {
    @NotBlank(message = "Company name cannot be blank")
    @Size(max = 100, message = "Company name cannot exceed 100 characters")
    private String nameCompany;

    @NotBlank(message = "Contact person name cannot be blank")
    @Size(max = 100, message = "Contact person name cannot exceed 100 characters")
    private String namePersonContact;

    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$", message = "Invalid phone number format")
    private String phone;

    @NotBlank(message = "License plate cannot be blank")
    @Size(max = 20, message = "License plate cannot exceed 20 characters")
    private String licensePlate;

    @Size(max = 255, message = "Note cannot exceed 255 characters")
    private String note;

    private String senderEmail;
}
