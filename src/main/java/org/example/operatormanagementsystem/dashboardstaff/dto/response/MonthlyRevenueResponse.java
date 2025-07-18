package org.example.operatormanagementsystem.dashboardstaff.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MonthlyRevenueResponse {
    private String month;
    private BigDecimal chuyenNha24H;
    private BigDecimal dvChuyenNhaSaiGon;
    private BigDecimal chuyenNhaMinhAnh;
}