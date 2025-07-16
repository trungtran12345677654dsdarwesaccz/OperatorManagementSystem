package org.example.operatormanagementsystem.managestaff_yen.controller;

import org.example.operatormanagementsystem.managestaff_yen.dto.request.UpdatePromotionRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.request.CancelPromotionRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.request.AddPromotionRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.response.*;
import org.example.operatormanagementsystem.managestaff_yen.service.PromotionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    @GetMapping
    public List<PromotionResponse> getAllPromotions(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status
    ) {
        return promotionService.searchPromotions(keyword, status);
    }

    @PostMapping("/update-dates")
    public PromotionResponse updatePromotionDates(@RequestBody UpdatePromotionRequest request) {
        return promotionService.updatePromotionDates(request);
    }

    @PostMapping("/update")
    public PromotionResponse updatePromotion(@RequestBody UpdatePromotionRequest request) {
        return promotionService.updatePromotion(request);
    }

    @PostMapping("/cancel")
    public PromotionResponse cancelPromotion(@RequestBody CancelPromotionRequest request) {
        return promotionService.cancelPromotion(request);
    }

    @PostMapping("/add")
    public PromotionResponse addPromotion(@RequestBody AddPromotionRequest request) {
        return promotionService.addPromotion(request);
    }

    @PostMapping("/update-description")
    public PromotionResponse updateDescription(@RequestBody UpdatePromotionRequest request) {
        return promotionService.updateDescription(request);
    }

    @GetMapping("/statistics/overview")
    public PromotionStatisticsResponse getPromotionOverview() {
        return promotionService.getPromotionOverview();
    }

    @GetMapping("/statistics/chart/revenue")
    public List<ChartDataPointResponse> getPromotionRevenue(
            @RequestParam String rangeType, // day, month, year
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return promotionService.getPromotionRevenue(rangeType, from, to);
    }

    @GetMapping("/statistics/chart/bookings")
    public List<ChartDataPointResponse> getPromotionBookingCount(
            @RequestParam String rangeType, // day, month, year
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return promotionService.getPromotionBookingCount(rangeType, from, to);
    }

    @GetMapping("/statistics/chart/status-ratio")
    public List<PieChartSegmentResponse> getPromotionStatusRatio() {
        return promotionService.getPromotionStatusRatio();
    }

    @GetMapping("/statistics/chart/feedback")
    public List<BarChartDataResponse> getPositiveFeedbackByPromotion() {
        return promotionService.getPositiveFeedbackByPromotion();
    }
}