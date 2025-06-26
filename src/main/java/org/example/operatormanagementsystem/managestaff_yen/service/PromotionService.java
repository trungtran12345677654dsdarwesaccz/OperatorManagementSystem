package org.example.operatormanagementsystem.managestaff_yen.service;

import org.example.operatormanagementsystem.managestaff_yen.dto.request.UpdatePromotionRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.request.CancelPromotionRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.request.AddPromotionRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.response.PromotionResponse;

import java.util.List;

public interface PromotionService {
    PromotionResponse updatePromotionDates(UpdatePromotionRequest request);
    PromotionResponse updatePromotion(UpdatePromotionRequest request);
    PromotionResponse cancelPromotion(CancelPromotionRequest request);

    List<PromotionResponse> searchPromotions(String keyword);

    PromotionResponse addPromotion(AddPromotionRequest request);

    PromotionResponse updateDescription(UpdatePromotionRequest request);
}