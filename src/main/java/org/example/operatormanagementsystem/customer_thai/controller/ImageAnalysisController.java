package org.example.operatormanagementsystem.customer_thai.controller;

import org.example.operatormanagementsystem.customer_thai.dto.ObjectDimensionsDTO;
import org.example.operatormanagementsystem.customer_thai.dto.ObjectsAnalysisResultDTO;
import org.example.operatormanagementsystem.customer_thai.service.ImageAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/api/customer")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('CUSTOMER')")
public class ImageAnalysisController {

    private static final Logger logger = LoggerFactory.getLogger(ImageAnalysisController.class);
    private final ImageAnalysisService imageAnalysisService;

    @Autowired
    public ImageAnalysisController(ImageAnalysisService imageAnalysisService) {
        this.imageAnalysisService = imageAnalysisService;
    }

    @PostMapping(value = "/dimensions", consumes = "multipart/form-data")
    @Operation(summary = "Phân tích kích thước vật thể từ ảnh",
               description = "Tải lên một file ảnh để phân tích và trả về kích thước ước tính.")
    public ResponseEntity<ObjectDimensionsDTO> analyzeObjectDimensions(
            @Parameter(description = "File ảnh cần phân tích (định dạng PNG, JPG/JPEG).", required = true)
            @RequestParam("image") MultipartFile imageFile) {
        
        logger.info("Received image analysis request for file: {}", imageFile.getOriginalFilename());

        // Kiểm tra file rỗng
        if (imageFile.isEmpty()) {
            logger.warn("Empty file received");
            return ResponseEntity.badRequest()
                .body(new ObjectDimensionsDTO(0.0, 0.0, 0.0, "File rỗng", 0.0));
        }

        // Kiểm tra định dạng file
        String contentType = imageFile.getContentType();
        if (contentType == null || !isImageFile(contentType)) {
            logger.warn("Invalid file type received: {}", contentType);
            return ResponseEntity.badRequest()
                .body(new ObjectDimensionsDTO(0.0, 0.0, 0.0, "Định dạng file không hợp lệ. Chỉ chấp nhận PNG, JPG, JPEG.", 0.0));
        }

        try {
            logger.info("Starting image analysis for file type: {}", contentType);
            ObjectDimensionsDTO dimensions = imageAnalysisService.analyzeObjectDimensions(imageFile);
            logger.info("Image analysis completed successfully");
            return ResponseEntity.ok(dimensions);
        } catch (IllegalArgumentException e) {
            // Lỗi tham số không hợp lệ
            logger.error("Invalid argument error during image analysis", e);
            return ResponseEntity.badRequest()
                .body(new ObjectDimensionsDTO(0.0, 0.0, 0.0, "Tham số không hợp lệ: " + e.getMessage(), 0.0));
        } catch (Exception e) {
            // Các lỗi khác (bao gồm lỗi từ Google Vision API)
            logger.error("Unexpected error during image analysis", e);
            return ResponseEntity.internalServerError()
                .body(new ObjectDimensionsDTO(0.0, 0.0, 0.0, "Lỗi hệ thống: " + e.getMessage(), 0.0));
        }
    }

    @PostMapping(value = "/all-dimensions", consumes = "multipart/form-data")
    @Operation(summary = "Phân tích kích thước tất cả vật thể từ ảnh",
               description = "Tải lên một file ảnh để phân tích và trả về kích thước ước tính của tất cả vật thể có trong ảnh.")
    public ResponseEntity<?> analyzeAllObjectsDimensions(
            @Parameter(description = "File ảnh cần phân tích (định dạng PNG, JPG/JPEG).", required = true)
            @RequestParam("image") MultipartFile imageFile) {
        
        logger.info("Received multi-object image analysis request for file: {}", imageFile.getOriginalFilename());

        // Kiểm tra file rỗng
        if (imageFile.isEmpty()) {
            logger.warn("Empty file received");
            return ResponseEntity.badRequest()
                .body(createErrorResponse("File rỗng"));
        }

        // Kiểm tra định dạng file
        String contentType = imageFile.getContentType();
        if (contentType == null || !isImageFile(contentType)) {
            logger.warn("Invalid file type received: {}", contentType);
            return ResponseEntity.badRequest()
                .body(createErrorResponse("Định dạng file không hợp lệ. Chỉ chấp nhận PNG, JPG, JPEG."));
        }

        try {
            logger.info("Starting multi-object image analysis for file type: {}", contentType);
            ObjectsAnalysisResultDTO result = imageAnalysisService.analyzeAllObjectsDimensions(imageFile);
            logger.info("Multi-object image analysis completed successfully. Found {} objects", result.getTotalObjects());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Unexpected error during multi-object image analysis", e);
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("Lỗi phân tích hình ảnh: " + e.getMessage()));
        }
    }

    private Map<String, Object> createErrorResponse(String errorMessage) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", errorMessage);
        response.put("objects", Collections.emptyList());
        response.put("totalObjects", 0);
        return response;
    }

    private boolean isImageFile(String contentType) {
        return contentType != null && (
            contentType.equals("image/jpeg") ||
            contentType.equals("image/png")
        );
    }
} 