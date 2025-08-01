package org.example.operatormanagementsystem.transportunit.controller;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.enumeration.UserStatus;
import org.example.operatormanagementsystem.transportunit.dto.request.TransportUnitRequest;
import org.example.operatormanagementsystem.transportunit.dto.request.TransportUnitSearchRequest;
import org.example.operatormanagementsystem.transportunit.dto.response.TransportUnitResponse;
import org.example.operatormanagementsystem.transportunit.service.TransportUnitService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus; // Import HttpStatus
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transport-units")
@RequiredArgsConstructor
public class TransportUnitController {

    private final TransportUnitService service;
    @GetMapping("/paged")
    @PreAuthorize("hasAnyRole('MANAGER')")
    public ResponseEntity<Page<TransportUnitResponse>> getAllPaged(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "transportId") String sortBy,
            @RequestParam(defaultValue = "asc") String dir) {

        return ResponseEntity.ok(
                service.getAllPaged(page, size, sortBy, dir)
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER')")
    public ResponseEntity<List<TransportUnitResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }


    @GetMapping("/search")  
    @PreAuthorize("hasAnyRole('MANAGER')")
    public ResponseEntity<List<TransportUnitResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(service.search(keyword));
    }

    @PostMapping("/search-advanced")
    @PreAuthorize("hasAnyRole('MANAGER')")
    public ResponseEntity<List<TransportUnitResponse>> searchAdvanced(@RequestBody TransportUnitSearchRequest request) {
        return ResponseEntity.ok(service.searchAdvanced(request));
    }

    @PutMapping("update/{id}")
    @PreAuthorize("hasRole('MANAGER')") // Đảm bảo annotation này được bật nếu bạn muốn phân quyền
    public ResponseEntity<TransportUnitResponse> update(
            @PathVariable Integer id,
            @RequestBody TransportUnitRequest request) {
        try {
            return ResponseEntity.ok(service.update(id, request));
        } catch (RuntimeException e) { // Bắt ngoại lệ "Not found" từ service
            System.err.println("Error updating Transport Unit: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Trả về 404
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER')") // Có thể thêm phân quyền nếu cần
    public ResponseEntity<TransportUnitResponse> getById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(service.getById(id));
        } catch (RuntimeException e) { // Bắt ngoại lệ "Not found" từ service
            System.err.println("Transport Unit not found for ID " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Trả về 404 Not Found
        }
    }


    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole( 'MANAGER')") // Cả STAFF và MANAGER đều có thể xem
    public ResponseEntity<List<TransportUnitResponse>> getByStatus(@PathVariable UserStatus status) {
        List<TransportUnitResponse> response = service.getByStatus(status);
        if (response.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204 No Content nếu không có kết quả
        }
        return ResponseEntity.ok(response);
    }


}
