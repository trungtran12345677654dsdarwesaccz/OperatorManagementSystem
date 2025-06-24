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
@CrossOrigin(origins = "http://localhost:5174")
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    /**
     * GET /api/promotions?keyword=...
     * - Nếu không có keyword -> trả toàn bộ danh sách
     * - Nếu có keyword -> trả các khuyến mãi có tiêu đề chứa keyword
     */
    @GetMapping
    public List<PromotionResponse> getAllPromotions(@RequestParam(value = "keyword", required = false) String keyword) {
        return promotionService.searchPromotions(keyword);
    }

    /**
     * POST /api/promotions/update-dates
     * - Cập nhật ngày bắt đầu / kết thúc
     */
    @PostMapping("/update-dates")
    public PromotionResponse updatePromotionDates(@RequestBody UpdatePromotionRequest request) {
        return promotionService.updatePromotionDates(request);
    }

    /**
     * POST /api/promotions/update
     * - Cập nhật thông tin khuyến mãi (title, description, v.v.)
     */
    @PostMapping("/update")
    public PromotionResponse updatePromotion(@RequestBody UpdatePromotionRequest request) {
        return promotionService.updatePromotion(request);
    }

    /**
     * POST /api/promotions/cancel
     * - Huỷ khuyến mãi
     */
    @PostMapping("/cancel")
    public PromotionResponse cancelPromotion(@RequestBody CancelPromotionRequest request) {
        return promotionService.cancelPromotion(request);
    }
}