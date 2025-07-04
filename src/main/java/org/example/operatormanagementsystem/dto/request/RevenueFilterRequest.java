package org.example.operatormanagementsystem.dto.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class RevenueFilterRequest {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
    
    private String beneficiaryType;
    private String sourceType;
    private Integer beneficiaryId;
    private Integer sourceId;
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "date";
    private String sortDirection = "desc";
} 