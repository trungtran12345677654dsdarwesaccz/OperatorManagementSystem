package org.example.operatormanagementsystem.customer_thai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransportFeedbackRequest {
    @NotNull(message = "Booking ID is required")
    private Integer bookingId;
    @NotBlank(message = "Feedback content is required")
    private String content;
    private Integer star;
    @NotNull(message = "Transport ID is required")
    private Integer transportId;
    
    @NotNull(message = "isTransport flag is required")
    private Boolean isTransport;
} 