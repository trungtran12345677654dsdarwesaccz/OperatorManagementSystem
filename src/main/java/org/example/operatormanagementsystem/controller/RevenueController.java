package org.example.operatormanagementsystem.controller;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.dto.request.RevenueFilterRequest;
import org.example.operatormanagementsystem.dto.response.PageResponse;
import org.example.operatormanagementsystem.dto.response.RevenueResponse;
import org.example.operatormanagementsystem.entity.Revenue;
import org.example.operatormanagementsystem.service.RevenueService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/revenues")
@RequiredArgsConstructor
// @PreAuthorize("hasAnyAuthority('STAFF', 'MANAGER')") // Temporarily disabled for testing
public class RevenueController {

    private final RevenueService revenueService;

    @GetMapping
    public ResponseEntity<List<RevenueResponse>> getAllRevenues() {
        List<RevenueResponse> revenues = revenueService.getAllRevenues();
        return ResponseEntity.ok(revenues);
    }

    @GetMapping("/filtered")
    public ResponseEntity<PageResponse<RevenueResponse>> getRevenuesWithFilters(RevenueFilterRequest filterRequest) {
        PageResponse<RevenueResponse> response = revenueService.getRevenuesWithFilters(filterRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public Revenue getRevenueById(@PathVariable Integer id) {
        return revenueService.getRevenueById(id);
    }

    @PostMapping
    public Revenue createRevenue(@RequestBody Revenue revenue) {
        return revenueService.createRevenue(revenue);
    }

    @PutMapping("/{id}")
    public Revenue updateRevenue(@PathVariable Integer id, @RequestBody Revenue revenue) {
        return revenueService.updateRevenue(id, revenue);
    }

    @DeleteMapping("/{id}")
    public void deleteRevenue(@PathVariable Integer id) {
        revenueService.deleteRevenue(id);
    }

    @GetMapping("/date-range")
    public List<Revenue> getRevenuesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return revenueService.getRevenuesByDateRange(startDate, endDate);
    }

    @GetMapping("/beneficiary/{beneficiaryId}")
    public List<Revenue> getRevenuesByBeneficiary(@PathVariable Integer beneficiaryId) {
        return revenueService.getRevenuesByBeneficiary(beneficiaryId);
    }

    @GetMapping("/source-type/{sourceType}")
    public List<Revenue> getRevenuesBySourceType(@PathVariable String sourceType) {
        return revenueService.getRevenuesBySourceType(sourceType);
    }

    @GetMapping("/booking/{bookingId}")
    public List<Revenue> getRevenuesByBooking(@PathVariable Integer bookingId) {
        return revenueService.getRevenuesByBooking(bookingId);
    }

    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getTotalRevenueBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        BigDecimal total = revenueService.getTotalRevenueBetweenDates(startDate, endDate);
        return ResponseEntity.ok(total != null ? total : BigDecimal.ZERO);
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportToExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String beneficiaryType,
            @RequestParam(required = false) String sourceType,
            @RequestParam(required = false) Integer beneficiaryId,
            @RequestParam(required = false) Integer sourceId) {
        byte[] excelFile = revenueService.exportToExcelWithFilters(startDate, endDate, beneficiaryType, sourceType, beneficiaryId, sourceId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "revenue_report.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelFile);
    }
}
