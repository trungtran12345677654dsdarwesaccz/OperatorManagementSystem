package org.example.operatormanagementsystem.managestaff_yen.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.example.operatormanagementsystem.enumeration.UserGender;
import org.example.operatormanagementsystem.enumeration.UserStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperatorStaffResponse {
    private Integer operatorId;
    private String fullName;
    private String username;
    private String email;
    private String phone;
    private String address;
    private UserGender gender;
    private UserStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private Long totalBookings;
    private Long totalFeedbacks;
    private Long totalChatbotLogs;
}
