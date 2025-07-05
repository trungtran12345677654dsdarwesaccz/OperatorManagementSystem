package org.example.operatormanagementsystem.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserUsageStatDto {
    private Long id;
    private Integer apiCallsToday;
    private LocalDate currentDate;
    private LocalDateTime lastLoginAt;
    private Integer loginCount;
    private Long totalOnlineSeconds;
    private Long userId;
}
