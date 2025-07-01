package org.example.operatormanagementsystem.transportunit.dto.response;

import org.example.operatormanagementsystem.enumeration.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusDistributionResponse {
    private UserStatus status;
    private long count;
    private double percentage;
}
