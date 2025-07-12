package org.example.operatormanagementsystem.managestaff_yen.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class RecentIssueResponse {
    private Integer issueId;
    private String description;
    private String status;
    private LocalDateTime createdAt;
}
