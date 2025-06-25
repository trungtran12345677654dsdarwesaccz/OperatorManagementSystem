package org.example.operatormanagementsystem.controller;

import lombok.RequiredArgsConstructor;
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
@PreAuthorize("hasRole('STAFF')")
public class RevenueController {

    private final RevenueService revenueService;

    @GetMapping
    public List<Revenue> getAllRevenues() {
        return revenueService.getAllRevenues();
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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate endDate) {
        // If dates are not provided, use current date
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        byte[] excelFile = revenueService.exportToExcel(startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "revenue_report.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelFile);
    }
}
