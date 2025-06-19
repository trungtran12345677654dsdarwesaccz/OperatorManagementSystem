package org.example.operatormanagementsystem.managestaff_yen.controller;

import org.example.operatormanagementsystem.managestaff_yen.dto.request.UpdatePromotionRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.request.CancelPromotionRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.response.PromotionResponse;
import org.example.operatormanagementsystem.managestaff_yen.service.PromotionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/promotions")
@CrossOrigin(origins = "http://localhost:5175")
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    @GetMapping
    public List<PromotionResponse> getAllPromotions() {
        return promotionService.getAllPromotions();
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
}
