package org.example.operatormanagementsystem.managestaff_yen.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.operatormanagementsystem.managestaff_yen.dto.request.ExportStaffRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.request.ManagerFeedbackRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.request.UpdateStaffRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.response.*;
import org.example.operatormanagementsystem.managestaff_yen.service.ExcelExportService;
import org.example.operatormanagementsystem.managestaff_yen.service.StaffManagementService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@CrossOrigin(origins = "http://localhost:5173")
@Slf4j
@RestController
@RequestMapping("/api/v1/manager/{managerId}/staff")
@RequiredArgsConstructor
@Tag(name = "Staff Management", description = "API endpoints for manager to manage operator staff")
public class StaffManagementController {

    private final StaffManagementService staffService;
    private final ExcelExportService excelExportService;

    @GetMapping
    @Operation(summary = "View staff information", description = "Get paginated list of staff managed by the manager")
    public ResponseEntity<ApiResponse<StaffListResponse>> viewStaffInformation(
            @PathVariable Integer managerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "users.fullName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            log.info("GET staff list for manager {}", managerId);
            var response = staffService.viewStaffInformation(managerId, page, size, sortBy, sortDir);
            return ResponseEntity.ok(ApiResponse.success("Staff list retrieved", response));
        } catch (Exception e) {
            return handleException("retrieving staff list", e);
        }
    }

    @GetMapping("/{operatorId}")
    @Operation(summary = "Get staff details", description = "Get detailed information of a specific staff member")
    public ResponseEntity<ApiResponse<OperatorStaffResponse>> getStaffDetails(
            @PathVariable Integer managerId,
            @PathVariable Integer operatorId) {
        try {
            log.info("Getting details for staff {} by manager {}", operatorId, managerId);
            var response = staffService.getStaffDetails(managerId, operatorId);
            return ResponseEntity.ok(ApiResponse.success("Staff details retrieved", response));
        } catch (Exception e) {
            return handleException("retrieving staff details", e);
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search staff", description = "Search staff by name, email, or username")
    public ResponseEntity<ApiResponse<StaffListResponse>> searchStaff(
            @PathVariable Integer managerId,
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("Searching staff for manager {} with '{}'", managerId, searchTerm);
            var response = staffService.searchStaff(managerId, searchTerm, null, null, page, size);
            return ResponseEntity.ok(ApiResponse.success("Search successful", response));
        } catch (Exception e) {
            return handleException("searching staff", e);
        }
    }

    @GetMapping("/filter")
    @Operation(summary = "Search staff with filters", description = "Search staff with filters like status or gender")
    public ResponseEntity<ApiResponse<StaffListResponse>> searchStaffWithFilters(
            @PathVariable Integer managerId,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String gender,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        try {
            var response = staffService.searchStaff(managerId, searchTerm, status, gender, page, size);
            return ResponseEntity.ok(ApiResponse.success("Filtered staff list retrieved", response));
        } catch (Exception e) {
            return handleException("filtering staff list", e);
        }
    }

    @PutMapping("/{operatorId}")
    @Operation(summary = "Update staff information", description = "Update basic staff information")
    public ResponseEntity<ApiResponse<OperatorStaffResponse>> updateStaff(
            @PathVariable Integer managerId,
            @PathVariable Integer operatorId,
            @Valid @RequestBody UpdateStaffRequest request) {
        try {
            log.info("Updating staff {} for manager {}", operatorId, managerId);
            var response = staffService.updateStaffInformation(managerId, operatorId, request);
            return ResponseEntity.ok(ApiResponse.success("Staff updated successfully", response));
        } catch (Exception e) {
            return handleException("updating staff", e);
        }
    }

    @PatchMapping("/{operatorId}/block")
    @Operation(summary = "Block staff", description = "Soft delete staff account by changing status to BLOCKED")
    public ResponseEntity<ApiResponse<Void>> blockStaff(
            @PathVariable Integer managerId,
            @PathVariable Integer operatorId) {
        try {
            log.info("Blocking staff {} by manager {}", operatorId, managerId);
            staffService.blockOrDeleteStaffAccount(managerId, operatorId, false);
            return ResponseEntity.ok(ApiResponse.success("Staff blocked successfully", null));
        } catch (Exception e) {
            return handleException("blocking staff", e);
        }
    }

    @PostMapping("/{operatorId}/feedback")
    @Operation(summary = "Send feedback", description = "Manager sends feedback to staff")
    public ResponseEntity<ApiResponse<Void>> feedbackToStaff(
            @PathVariable Integer managerId,
            @PathVariable Integer operatorId,
            @Valid @RequestBody ManagerFeedbackRequest request) {
        try {
            log.info("Manager {} sending feedback to staff {}", managerId, operatorId);
            staffService.feedbackToStaff(managerId, operatorId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Feedback sent successfully", null));
        } catch (Exception e) {
            return handleException("sending feedback", e);
        }
    }

    @GetMapping("/overview")
    @Operation(summary = "Get staff overview", description = "Get summary statistics for staff under manager")
    public ResponseEntity<ApiResponse<StaffOverviewResponse>> getStaffOverview(
            @PathVariable Integer managerId) {
        try {
            log.info("Getting staff overview for manager {}", managerId);
            var response = staffService.getStaffOverview(managerId);
            return ResponseEntity.ok(ApiResponse.success("Overview retrieved", response));
        } catch (Exception e) {
            return handleException("retrieving staff overview", e);
        }
    }

    @PostMapping("/export")
    @Operation(summary = "Export staff to Excel", description = "Export staff list to Excel file")
    public ResponseEntity<byte[]> exportStaffToExcel(
            @PathVariable Integer managerId,
            @RequestBody(required = false) ExportStaffRequest request) {
        try {
            log.info("Exporting staff to Excel for manager {}", managerId);
            if (request == null) {
                request = ExportStaffRequest.builder().build();
            }
            byte[] excelData = staffService.exportStaffToExcel(managerId, request);
            String filename = excelExportService.generateFileName(managerId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);

        } catch (IOException e) {
            log.error("Error exporting staff to Excel for manager {}", managerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error exporting to Excel: " + e.getMessage()).getBytes());
        }
    }

    private <T> ResponseEntity<ApiResponse<T>> handleException(String action, Exception e) {
        log.error("Error while {}", action, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed while " + action + ": " + e.getMessage()));
    }
    @GetMapping("/performance")
    @Operation(summary = "Get staff performance overview", description = "Includes new staff per month, top bookings, feedbacks")
    public ResponseEntity<ApiResponse<StaffPerformanceOverviewResponse>> getStaffPerformance(
            @PathVariable Integer managerId) {
        try {
            var response = staffService.getStaffPerformanceOverview(managerId);
            return ResponseEntity.ok(ApiResponse.success("Performance overview loaded", response));
        } catch (Exception e) {
            return handleException("getting performance overview", e);
        }
    }

}
