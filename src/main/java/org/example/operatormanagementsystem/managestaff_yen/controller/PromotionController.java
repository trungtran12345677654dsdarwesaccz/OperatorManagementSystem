package org.example.operatormanagementsystem.managestaff_yen.controller;

import org.example.operatormanagementsystem.managestaff_yen.dto.request.UpdatePromotionRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.request.CancelPromotionRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.request.AddPromotionRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.response.PromotionResponse;
import org.example.operatormanagementsystem.managestaff_yen.service.PromotionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@CrossOrigin(origins = "http://localhost:5173")
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    @GetMapping
    public List<PromotionResponse> getAllPromotions(@RequestParam(value = "keyword", required = false) String keyword) {
        return promotionService.searchPromotions(keyword);
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
}