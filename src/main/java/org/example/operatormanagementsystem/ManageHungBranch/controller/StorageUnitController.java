package org.example.operatormanagementsystem.ManageHungBranch.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.operatormanagementsystem.ManageHungBranch.dto.StorageUnitDTO;
import org.example.operatormanagementsystem.ManageHungBranch.service.StorageUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/storage-units")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('STAFF')")
@Tag(name = "Storage Unit Management", description = "API quản lý kho lưu trữ - Manage Storage Unit")
public class StorageUnitController {

    @Autowired
    private StorageUnitService storageUnitService;

    @Operation(summary = "Get Storage Units with Images",
            description = "Lấy danh sách các kho lưu trữ có ảnh")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    @GetMapping("/with-images")
    public ResponseEntity<List<StorageUnitDTO>> getStorageUnitsWithImage() {
        log.info("GET /api/storage-units/with-images - Lấy storage units có ảnh");
        try {
            List<StorageUnitDTO> storageUnits = storageUnitService.getStorageUnitsWithImage();
            return ResponseEntity.ok(storageUnits);
        } catch (Exception e) {
            log.error("Lỗi khi lấy storage units có ảnh: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get Storage Units without Images",
            description = "Lấy danh sách các kho lưu trữ không có ảnh")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    @GetMapping("/without-images")
    public ResponseEntity<List<StorageUnitDTO>> getStorageUnitsWithoutImage() {
        log.info("GET /api/storage-units/without-images - Lấy storage units không có ảnh");
        try {
            List<StorageUnitDTO> storageUnits = storageUnitService.getStorageUnitsWithoutImage();
            return ResponseEntity.ok(storageUnits);
        } catch (Exception e) {
            log.error("Lỗi khi lấy storage units không có ảnh: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Advanced Search Storage Units",
            description = "Tìm kiếm kho lưu trữ theo các điều kiện bao gồm cả điều kiện có/không có ảnh")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tìm kiếm thành công"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    @GetMapping("/advanced-search")
    public ResponseEntity<List<StorageUnitDTO>> advancedSearchStorageUnits(
            @Parameter(description = "Tên kho (tìm kiếm gần đúng)") @RequestParam(required = false) String name,
            @Parameter(description = "Địa chỉ (tìm kiếm gần đúng)") @RequestParam(required = false) String address,
            @Parameter(description = "Trạng thái") @RequestParam(required = false) String status,
            @Parameter(description = "ID của manager") @RequestParam(required = false) Integer managerId,
            @Parameter(description = "Có ảnh hay không (true: có ảnh, false: không có ảnh)") @RequestParam(required = false) Boolean hasImage) {
        log.info("GET /api/storage-units/advanced-search - Tìm kiếm nâng cao với điều kiện: name={}, address={}, status={}, managerId={}, hasImage={}",
                name, address, status, managerId, hasImage);
        try {
            List<StorageUnitDTO> storageUnits = storageUnitService.searchStorageUnits(name, address, status, managerId, hasImage);
            return ResponseEntity.ok(storageUnits);
        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm nâng cao storage units: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Update Storage Unit Image",
            description = "Cập nhật ảnh cho kho lưu trữ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật ảnh thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy storage unit"),
            @ApiResponse(responseCode = "400", description = "URL ảnh không hợp lệ"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    @PatchMapping("/{id}/image")
    public ResponseEntity<StorageUnitDTO> updateStorageUnitImage(
            @Parameter(description = "ID của storage unit") @PathVariable Integer id,
            @Parameter(description = "URL ảnh mới") @RequestParam String imageUrl) {
        log.info("PATCH /api/storage-units/{}/image - Cập nhật ảnh cho storage unit", id);
        try {
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            StorageUnitDTO updatedStorageUnit = storageUnitService.updateStorageUnitImage(id, imageUrl.trim());
            return ResponseEntity.ok(updatedStorageUnit);
        } catch (RuntimeException e) {
            log.error("Lỗi khi cập nhật ảnh cho storage unit với ID {}: ", id, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Lỗi server khi cập nhật ảnh cho storage unit với ID {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Remove Storage Unit Image",
            description = "Xóa ảnh của kho lưu trữ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa ảnh thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy storage unit"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    @DeleteMapping("/{id}/image")
    public ResponseEntity<StorageUnitDTO> removeStorageUnitImage(
            @Parameter(description = "ID của storage unit") @PathVariable Integer id) {
        log.info("DELETE /api/storage-units/{}/image - Xóa ảnh của storage unit", id);
        try {
            StorageUnitDTO updatedStorageUnit = storageUnitService.removeStorageUnitImage(id);
            return ResponseEntity.ok(updatedStorageUnit);
        } catch (RuntimeException e) {
            log.error("Lỗi khi xóa ảnh của storage unit với ID {}: ", id, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Lỗi server khi xóa ảnh của storage unit với ID {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Upload Storage Unit Image",
            description = "Upload ảnh cho kho lưu trữ (multipart file)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upload ảnh thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy storage unit"),
            @ApiResponse(responseCode = "400", description = "File không hợp lệ"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    @PostMapping("/{id}/upload-image")
    public ResponseEntity<StorageUnitDTO> uploadStorageUnitImage(
            @Parameter(description = "ID của storage unit") @PathVariable Integer id,
            @Parameter(description = "File ảnh cần upload") @RequestParam("file") MultipartFile file) {
        log.info("POST /api/storage-units/{}/upload-image - Upload ảnh cho storage unit", id);
        try {
            // Kiểm tra file có hợp lệ không
            if (file.isEmpty()) {
                log.warn("File upload rỗng cho storage unit ID: {}", id);
                return ResponseEntity.badRequest().build();
            }

            // Kiểm tra định dạng file (chỉ cho phép ảnh)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                log.warn("File không phải là ảnh cho storage unit ID: {}, contentType: {}", id, contentType);
                return ResponseEntity.badRequest().build();
            }

            // Kiểm tra kích thước file (giới hạn 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                log.warn("File quá lớn cho storage unit ID: {}, size: {} bytes", id, file.getSize());
                return ResponseEntity.badRequest().build();
            }

            // TODO: Implement file upload logic here
            // Ví dụ: lưu file vào thư mục uploads hoặc cloud storage
            // String imageUrl = fileUploadService.uploadFile(file);

            // Tạm thời sử dụng tên file làm URL (cần implement thực tế)
            String imageUrl = "/uploads/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();

            StorageUnitDTO updatedStorageUnit = storageUnitService.updateStorageUnitImage(id, imageUrl);
            return ResponseEntity.ok(updatedStorageUnit);

        } catch (RuntimeException e) {
            log.error("Lỗi khi upload ảnh cho storage unit với ID {}: ", id, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Lỗi server khi upload ảnh cho storage unit với ID {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
