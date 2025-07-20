package org.example.operatormanagementsystem.customer_thai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObjectDimensionsDTO {
    private Double length;
    private Double width;
    private Double height;
    private String objectName;
    private Double confidence;
    
    public ObjectDimensionsDTO(Double length, Double width, Double height) {
        this.length = length;
        this.width = width;
        this.height = height;
    }
} 