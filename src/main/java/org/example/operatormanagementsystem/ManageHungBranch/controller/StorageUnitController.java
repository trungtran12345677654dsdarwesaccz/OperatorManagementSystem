package org.example.operatormanagementsystem.ManageHungBranch.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.operatormanagementsystem.ManageHungBranch.dto.CreateStorageUnitDTO;
import org.example.operatormanagementsystem.ManageHungBranch.dto.StorageUnitDTO;
import org.example.operatormanagementsystem.ManageHungBranch.service.StorageUnitService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.example.operatormanagementsystem.config.CloudinaryService;


@RestController
@RequestMapping("/api/storage-units")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Storage Unit Management", description = "API quản lý kho lưu trữ - Manage Storage Unit")
public class StorageUnitController {

    private final StorageUnitService storageUnitService;
    private final CloudinaryService cloudinaryService;


    private static final String UPLOAD_DIR = "uploads/";

    @PostConstruct
    public void init() throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
    }

    @Operation(summary = "Upload Storage Unit Image",
            description = "Tải lên ảnh cho kho lưu trữ và trả về URL")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tải ảnh thành công"),
            @ApiResponse(responseCode = "400", description = "Không có file hoặc định dạng file không hợp lệ"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ROLE_STAFF')")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("No file uploaded");
            }

            String fileName = UUID.randomUUID().toString();
            String imageUrl = cloudinaryService.uploadImage(file.getBytes(), fileName);
            return ResponseEntity.ok(new ImageResponse(imageUrl));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload failed: " + e.getMessage());
        }
    }

    @Data
    static class ImageResponse {
        private String imageUrl;

        public ImageResponse(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }

    @Operation(summary = "View Storage Unit Information",
            description = "Lấy danh sách tất cả các kho lưu trữ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    @GetMapping
    @PreAuthorize("hasRole('ROLE_STAFF')")
    public ResponseEntity<List<StorageUnitDTO>> getAllStorageUnits() {
        log.info("GET /api/storage-units - Lấy tất cả storage units");
        try {
            List<StorageUnitDTO> storageUnits = storageUnitService.getAllStorageUnits();
            return ResponseEntity.ok(storageUnits);
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách storage units: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "View Storage Unit Information by ID",
            description = "Lấy thông tin chi tiết của một kho lưu trữ theo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy storage unit"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_STAFF')")
    public ResponseEntity<StorageUnitDTO> getStorageUnitById(
            @Parameter(description = "ID của storage unit") @PathVariable Integer id) {
        log.info("GET /api/storage-units/{} - Lấy storage unit theo ID", id);
        try {
            Optional<StorageUnitDTO> storageUnit = storageUnitService.getStorageUnitById(id);
            return storageUnit.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Lỗi khi lấy storage unit với ID {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Add Storage Unit",
            description = "Thêm mới một kho lưu trữ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo storage unit thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    @PostMapping
    @PreAuthorize("hasRole('ROLE_STAFF')")
    public ResponseEntity<StorageUnitDTO> createStorageUnit(
            @Valid @RequestBody CreateStorageUnitDTO createDTO) {
        log.info("POST /api/storage-units - Tạo mới storage unit: {}", createDTO.getName());
        try {
            StorageUnitDTO createdStorageUnit = storageUnitService.createStorageUnit(createDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdStorageUnit);
        } catch (RuntimeException e) {
            log.error("Lỗi khi tạo storage unit: ", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Lỗi server khi tạo storage unit: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @Operation(summary = "Update Storage Unit Information",
            description = "Cập nhật thông tin của một kho lưu trữ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy storage unit"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_STAFF')")
    public ResponseEntity<StorageUnitDTO> updateStorageUnit(
            @Parameter(description = "ID của storage unit cần cập nhật") @PathVariable Integer id,
            @Parameter(description = "Thông tin cập nhật")
            @Valid @RequestBody StorageUnitDTO storageUnitDTO) {
        log.info("PUT /api/storage-units/{} - Cập nhật storage unit", id);
        try {
            StorageUnitDTO updatedStorageUnit = storageUnitService.updateStorageUnit(id, storageUnitDTO);
            return ResponseEntity.ok(updatedStorageUnit);
        } catch (RuntimeException e) {
            log.error("Lỗi khi cập nhật storage unit với ID {}: ", id, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Lỗi server khi cập nhật storage unit với ID {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Delete Storage Unit",
            description = "Xóa một kho lưu trữ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Xóa thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy storage unit"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_STAFF')")
    public ResponseEntity<Void> deleteStorageUnit(
            @Parameter(description = "ID của storage unit cần xóa") @PathVariable Integer id) {
        log.info("DELETE /api/storage-units/{} - Xóa storage unit", id);
        try {
            storageUnitService.deleteStorageUnit(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Lỗi khi xóa storage unit với ID {}: ", id, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Lỗi server khi xóa storage unit với ID {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Search Storage Units",
            description = "Tìm kiếm kho lưu trữ theo các điều kiện")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tìm kiếm thành công"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    @GetMapping("/search")
    @PreAuthorize("hasRole('ROLE_STAFF')")
    public ResponseEntity<List<StorageUnitDTO>> searchStorageUnits(
            @Parameter(description = "Tên kho (tìm kiếm gần đúng)") @RequestParam(required = false) String name,
            @Parameter(description = "Địa chỉ (tìm kiếm gần đúng)") @RequestParam(required = false) String address,
            @Parameter(description = "Trạng thái") @RequestParam(required = false) String status,
            @Parameter(description = "ID của manager") @RequestParam(required = false) Integer managerId) {
        log.info("GET /api/storage-units/search - Tìm kiếm với điều kiện: name={}, address={}, status={}, managerId={}",
                name, address, status, managerId);
        try {
            List<StorageUnitDTO> storageUnits = storageUnitService.searchStorageUnits(name, address, status, managerId);
            return ResponseEntity.ok(storageUnits);
        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm storage units: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Choose Storage Units to store",
            description = "Lấy danh sách kho lưu trữ theo manager ID để lựa chọn nơi lưu trữ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    @GetMapping("/by-manager/{managerId}")
    @PreAuthorize("hasRole('ROLE_STAFF')")
    public ResponseEntity<List<StorageUnitDTO>> getStorageUnitsByManager(
            @Parameter(description = "ID của manager") @PathVariable Integer managerId) {
        log.info("GET /api/storage-units/by-manager/{} - Lấy storage units theo manager", managerId);
        try {
            List<StorageUnitDTO> storageUnits = storageUnitService.getStorageUnitsByManagerId(managerId);
            return ResponseEntity.ok(storageUnits);
        } catch (Exception e) {
            log.error("Lỗi khi lấy storage units theo manager ID {}: ", managerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}