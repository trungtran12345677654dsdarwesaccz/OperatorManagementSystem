package org.example.operatormanagementsystem.managestaff_yen.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopOperatorFilterRequest {
    private int limit;
    private LocalDate fromDate;
    private LocalDate toDate;
}