package org.example.operatormanagementsystem.managestaff_yen.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffPerformanceResponse {
    private Integer operatorId;
    private String fullName;
    private int bookingCount;
    private int goodFeedbackCount;
    private int loginCount;
    private int issueCount;
    private int performanceScore;
    private String performanceLevel;
    private String email;

}