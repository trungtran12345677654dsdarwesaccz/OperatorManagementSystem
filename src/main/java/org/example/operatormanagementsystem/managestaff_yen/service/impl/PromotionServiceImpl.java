package org.example.operatormanagementsystem.managestaff_yen.service.impl;

import org.example.operatormanagementsystem.entity.Promotion;
import org.example.operatormanagementsystem.managestaff_yen.dto.request.UpdatePromotionRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.request.CancelPromotionRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.request.AddPromotionRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.response.PromotionResponse;
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
            if (request.getStatus() != null) {
                promotion.setStatus(request.getStatus());
            }
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

    @Override
    public List<PromotionResponse> searchPromotions(String keyword, String status) {
        List<Promotion> promotions = promotionRepository.searchByKeywordAndStatus(keyword, status);
        return promotions.stream().map(p -> {
            PromotionResponse res = new PromotionResponse();
            res.setId(p.getId());
            res.setName(p.getName());
            res.setStartDate(p.getStartDate());
            res.setEndDate(p.getEndDate());
            res.setStatus(p.getStatus());
            return res;
        }).collect(Collectors.toList());
    }

    @Override
    public PromotionResponse addPromotion(AddPromotionRequest request) {
        Promotion promotion = new Promotion();
        promotion.setName(request.getName());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setStatus(request.getStatus() != null ? request.getStatus() : "active");
        promotion = promotionRepository.save(promotion);

        PromotionResponse response = new PromotionResponse();
        response.setId(promotion.getId());
        response.setName(promotion.getName());
        response.setStartDate(promotion.getStartDate());
        response.setEndDate(promotion.getEndDate());
        response.setStatus("success");
        response.setMessage("Promotion added successfully");
        return response;
    }

    @Override
    public PromotionResponse updateDescription(UpdatePromotionRequest request) {
        PromotionResponse response = new PromotionResponse();
        Optional<Promotion> promotionOpt = promotionRepository.findById(request.getId());
        if (promotionOpt.isPresent()) {
            Promotion promotion = promotionOpt.get();
            promotion.setDescription(request.getDescription());
            promotionRepository.save(promotion);
            response.setId(promotion.getId());
            response.setStatus("success");
            response.setMessage("Promotion description updated successfully");
        } else {
            response.setStatus("error");
            response.setMessage("Promotion not found");
        }
        return response;
    }

    @Override
    public List<PromotionResponse> getAllPromotions() {
        return searchPromotions(null, null);
    }
}