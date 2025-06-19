package org.example.operatormanagementsystem.managestaff_yen.controller;


import io.swagger.v3.oas.annotations.*;
import org.example.operatormanagementsystem.managestaff_yen.dto.response.ApiResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.operatormanagementsystem.managestaff_yen.dto.request.ManagerFeedbackRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.request.UpdateStaffRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.response.*;
import org.example.operatormanagementsystem.managestaff_yen.service.StaffManagementService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5175")
@Slf4j
@RestController
@RequestMapping("/api/v1/manager/{managerId}/staff")
@RequiredArgsConstructor
@Tag(name = "Staff Management", description = "API endpoints for manager to manage operator staff")
public class StaffManagementController {

    private final StaffManagementService staffManagementService;

    // === View Staff List ===
    @Operation(summary = "View staff information", description = "Get paginated list of staff managed by the manager")
    @GetMapping
    public ResponseEntity<ApiResponse<StaffListResponse>> viewStaffInformation(
            @PathVariable Integer managerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "users.fullName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        try {
            log.info("GET staff list for manager {}", managerId);
            var response = staffManagementService.viewStaffInformation(managerId, page, size, sortBy, sortDir);
            return ResponseEntity.ok(ApiResponse.success("Staff list retrieved", response));
        } catch (Exception e) {
            return handleException("retrieving staff list", e);
        }
    }

    // === Search Staff ===
    @Operation(summary = "Search staff", description = "Search staff by name, email, or username")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<StaffListResponse>> searchStaff(
            @PathVariable Integer managerId,
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            log.info("Searching staff for manager {} with '{}'", managerId, searchTerm);
            var response = staffManagementService.searchStaff(managerId, searchTerm, page, size);
            return ResponseEntity.ok(ApiResponse.success("Search successful", response));
        } catch (Exception e) {
            return handleException("searching staff", e);
        }
    }

    // === Update Staff ===
    @Operation(summary = "Update staff information", description = "Update basic staff information")
    @PutMapping("/{operatorId}")
    public ResponseEntity<ApiResponse<OperatorStaffResponse>> updateStaff(
            @PathVariable Integer managerId,
            @PathVariable Integer operatorId,
            @Valid @RequestBody UpdateStaffRequest request) {

        try {
            log.info("Updating staff {} for manager {}", operatorId, managerId);
            var response = staffManagementService.updateStaffInformation(managerId, operatorId, request);
            return ResponseEntity.ok(ApiResponse.success("Staff updated successfully", response));
        } catch (Exception e) {
            return handleException("updating staff", e);
        }
    }

    // === Block Staff ===
    @Operation(summary = "Block staff", description = "Soft delete staff account by changing status to BLOCKED")
    @PatchMapping("/{operatorId}/block")
    public ResponseEntity<ApiResponse<Void>> blockStaff(
            @PathVariable Integer managerId,
            @PathVariable Integer operatorId) {

        try {
            log.info("Blocking staff {} by manager {}", operatorId, managerId);
            staffManagementService.blockOrDeleteStaffAccount(managerId, operatorId, false);
            return ResponseEntity.ok(ApiResponse.success("Staff blocked successfully", null));
        } catch (Exception e) {
            return handleException("blocking staff", e);
        }
    }

    // === Delete Staff ===
    @Operation(summary = "Delete staff", description = "Permanently delete staff account")
    @DeleteMapping("/{operatorId}")
    public ResponseEntity<ApiResponse<Void>> deleteStaff(
            @PathVariable Integer managerId,
            @PathVariable Integer operatorId) {

        try {
            log.info("Deleting staff {} by manager {}", operatorId, managerId);
            staffManagementService.blockOrDeleteStaffAccount(managerId, operatorId, true);
            return ResponseEntity.ok(ApiResponse.success("Staff deleted successfully", null));
        } catch (Exception e) {
            return handleException("deleting staff", e);
        }
    }

    // === Send Feedback ===
    @Operation(summary = "Send feedback", description = "Manager sends feedback to staff")
    @PostMapping("/{operatorId}/feedback")
    public ResponseEntity<ApiResponse<Void>> feedbackToStaff(
            @PathVariable Integer managerId,
            @PathVariable Integer operatorId,
            @Valid @RequestBody ManagerFeedbackRequest request) {

        try {
            log.info("Manager {} sending feedback to staff {}", managerId, operatorId);
            staffManagementService.feedbackToStaff(managerId, operatorId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Feedback sent successfully", null));
        } catch (Exception e) {
            return handleException("sending feedback", e);
        }
    }

    // === Staff Overview ===
    @Operation(summary = "Get staff overview", description = "Get summary statistics for staff under manager")
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<StaffOverviewResponse>> getStaffOverview(
            @PathVariable Integer managerId) {

        try {
            log.info("Getting staff overview for manager {}", managerId);
            var response = staffManagementService.getStaffOverview(managerId);
            return ResponseEntity.ok(ApiResponse.success("Overview retrieved", response));
        } catch (Exception e) {
            return handleException("retrieving staff overview", e);
        }
    }

    // === Common Error Handler ===
    private <T> ResponseEntity<ApiResponse<T>> handleException(String action, Exception e) {
        log.error("Error while {}", action, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed while " + action + ": " + e.getMessage()));
    }
}
