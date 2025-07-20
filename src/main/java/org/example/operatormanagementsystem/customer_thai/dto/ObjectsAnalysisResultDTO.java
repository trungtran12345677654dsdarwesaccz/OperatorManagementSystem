package org.example.operatormanagementsystem.customer_thai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObjectsAnalysisResultDTO {
    private List<ObjectDimensionsDTO> objects;
    private int totalObjects;
} 