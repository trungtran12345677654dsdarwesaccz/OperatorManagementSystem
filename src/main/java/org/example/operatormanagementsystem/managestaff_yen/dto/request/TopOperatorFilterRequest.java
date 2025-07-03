package org.example.operatormanagementsystem.managestaff_yen.dto.request;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopOperatorFilterRequest {
    private Integer managerId;
    private int limit;         // Số operator cần lấy
    private String sortBy;     // successOrders | onTimeRate
}
