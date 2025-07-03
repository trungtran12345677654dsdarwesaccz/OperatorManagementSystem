package org.example.operatormanagementsystem.managestaff_yen.dto.request;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChartFilterRequest {
    private String type;   // orders | revenue
    private String range;  // today | week | month
}
