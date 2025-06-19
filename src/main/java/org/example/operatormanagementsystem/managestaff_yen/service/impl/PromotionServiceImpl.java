package org.example.operatormanagementsystem.managestaff_yen.service.impl;

import org.example.operatormanagementsystem.managestaff_yen.dto.request.UpdatePromotionRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.request.CancelPromotionRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.response.PromotionResponse;
import org.example.operatormanagementsystem.entity.Promotion;
import org.example.operatormanagementsystem.managestaff_yen.repository.PromotionRepository;
import org.example.operatormanagementsystem.managestaff_yen.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PromotionServiceImpl implements PromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    @Override
    public PromotionResponse updatePromotionDates(UpdatePromotionRequest request) {
        PromotionResponse response = new PromotionResponse();
        Optional<Promotion> promotionOpt = promotionRepository.findById(request.getId());
        if (promotionOpt.isPresent()) {
            Promotion promotion = promotionOpt.get();
            promotion.setStartDate(request.getStartDate());
            promotion.setEndDate(request.getEndDate());
            promotionRepository.save(promotion);
            response.setId(promotion.getId());
            response.setStatus("success");
            response.setMessage("Promotion dates updated successfully");
        } else {
            response.setStatus("error");
            response.setMessage("Promotion not found");
        }
        return response;
    }

    @Override
    public PromotionResponse updatePromotion(UpdatePromotionRequest request) {
        PromotionResponse response = new PromotionResponse();
        Optional<Promotion> promotionOpt = promotionRepository.findById(request.getId());
        if (promotionOpt.isPresent()) {
            Promotion promotion = promotionOpt.get();
            promotion.setName(request.getName());
            promotionRepository.save(promotion);
            response.setId(promotion.getId());
            response.setStatus("success");
            response.setMessage("Promotion updated successfully");
        } else {
            response.setStatus("error");
            response.setMessage("Promotion not found");
        }
        return response;
    }

    @Override
    public PromotionResponse cancelPromotion(CancelPromotionRequest request) {
        PromotionResponse response = new PromotionResponse();
        Optional<Promotion> promotionOpt = promotionRepository.findById(request.getId());
        if (promotionOpt.isPresent()) {
            Promotion promotion = promotionOpt.get();
            promotion.setStatus("cancelled");
            promotionRepository.save(promotion);
            response.setId(promotion.getId());
            response.setStatus("success");
            response.setMessage("Promotion cancelled successfully");
        } else {
            response.setStatus("error");
            response.setMessage("Promotion not found");
        }
        return response;
    }

    // ✅ New method to get all promotions
    @Override
    public List<PromotionResponse> getAllPromotions() {
        return promotionRepository.findAll().stream()
                .map(promotion -> {
                    PromotionResponse response = new PromotionResponse();
                    response.setId(promotion.getId());
                    response.setName(promotion.getName());
                    response.setStartDate(promotion.getStartDate());
                    response.setEndDate(promotion.getEndDate());
                    response.setStatus(promotion.getStatus());
                    return response;
                })
                .collect(Collectors.toList());
    }
}
