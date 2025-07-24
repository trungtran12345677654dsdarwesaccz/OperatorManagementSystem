package org.example.operatormanagementsystem.controller;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.dto.response.RevenueResponse;
import org.example.operatormanagementsystem.entity.Revenue;
import org.example.operatormanagementsystem.service.RevenueService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
@PreAuthorize("hasRole('MANAGER')")
public class RevenueController {

    private final RevenueService revenueService;

    @GetMapping
    public ResponseEntity<List<RevenueResponse>> getAllRevenues() {
        List<RevenueResponse> revenues = revenueService.getAllRevenues();
        return ResponseEntity.ok(revenues);
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
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate endDate) {
        BigDecimal total = revenueService.getTotalRevenueBetweenDates(startDate, endDate);
        return ResponseEntity.ok(total != null ? total : BigDecimal.ZERO);
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportToExcel(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String sourceType,
            @RequestParam(required = false) String beneficiaryId,
            @RequestParam(required = false) String bookingId,
            @RequestParam(required = false) String minAmount,
            @RequestParam(required = false) String maxAmount
    ) {
        byte[] excelFile = revenueService.exportToExcelWithFilter(startDate, endDate, sourceType, beneficiaryId, bookingId, minAmount, maxAmount);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "revenue_report.xlsx");
        return ResponseEntity.ok()
                .headers(headers)
                .body(excelFile);
    }

    // Add this endpoint for paginated/filterable revenues
    @GetMapping("/filtered")
    public Page<RevenueResponse> getPagedRevenues(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String sourceType,
            @RequestParam(required = false) String beneficiaryId,
            @RequestParam(required = false) String bookingId,
            @RequestParam(required = false) String minAmount,
            @RequestParam(required = false) String maxAmount
    ) {
        Pageable pageable = PageRequest.of(page, size);
        // Implement this method in RevenueService to handle filtering and paging
        return revenueService.getPagedRevenues(pageable, startDate, endDate, sourceType, beneficiaryId, bookingId, minAmount, maxAmount);
    }
}
