package org.example.operatormanagementsystem.dto.response;

import lombok.*;
import org.example.operatormanagementsystem.enumeration.TimeOffStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeOffStatusResponse {
    private Integer requestId;
    private Integer operatorId;
    private String operatorName;
    private String operatorEmail;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private TimeOffStatus status;
    private String managerComments;
    private LocalDateTime requestDate;
    private LocalDateTime reviewedDate;
    private String reviewedByName;
    private Integer reviewedById;
    private int totalDays;
    private boolean hasConflicts;
    private String conflictDetails;
}