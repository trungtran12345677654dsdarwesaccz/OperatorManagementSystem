package org.example.operatormanagementsystem.managestaff_yen.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChartFilterRequest {
    private String type;   // orders | revenue
    private String range;  // today | week | month | year | range
    private LocalDate fromDate; // dùng khi range = "range"
    private LocalDate toDate;
}
