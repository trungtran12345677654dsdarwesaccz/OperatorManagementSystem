package org.example.operatormanagementsystem.managestaff_yen.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class ChartDataPointResponse {
    private LocalDate date;
    private int value;
}
