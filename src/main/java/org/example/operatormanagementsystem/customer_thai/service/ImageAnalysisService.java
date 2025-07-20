package org.example.operatormanagementsystem.customer_thai.service;

import org.example.operatormanagementsystem.customer_thai.dto.ObjectDimensionsDTO;
import org.example.operatormanagementsystem.customer_thai.dto.ObjectsAnalysisResultDTO;
import org.springframework.web.multipart.MultipartFile;

public interface ImageAnalysisService {
    ObjectDimensionsDTO analyzeObjectDimensions(MultipartFile imageFile);
    ObjectsAnalysisResultDTO analyzeAllObjectsDimensions(MultipartFile imageFile);
} 