package org.example.operatormanagementsystem.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder@AllArgsConstructor@NoArgsConstructor
public class UserActivityLogResponse {
    private String action;
    private String metadata;
    private LocalDateTime timestamp;
}
