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

    // ✅ Refactor: hỗ trợ tìm kiếm theo keyword
    @Override
    public List<PromotionResponse> searchPromotions(String keyword) {
        List<Promotion> promotions;
        if (keyword != null && !keyword.trim().isEmpty()) {
            promotions = promotionRepository.findByNameContainingIgnoreCase(keyword);
        } else {
            promotions = promotionRepository.findAll();
        }

        return promotions.stream()
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

    // ✅ Thêm mới khuyến mãi
    @Override
    public PromotionResponse addPromotion(AddPromotionRequest request) {
        PromotionResponse response = new PromotionResponse();
        Promotion promotion = new Promotion();
        promotion.setName(request.getName());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setStatus("active"); // Mặc định trạng thái là active
        promotion = promotionRepository.save(promotion);

        response.setId(promotion.getId());
        response.setName(promotion.getName());
        response.setStartDate(promotion.getStartDate());
        response.setEndDate(promotion.getEndDate());
        response.setStatus("success");
        response.setMessage("Promotion added successfully");
        return response;
    }

    // ✅ Cập nhật mô tả khuyến mãi
    @Override
    public PromotionResponse updateDescription(UpdatePromotionRequest request) {
        PromotionResponse response = new PromotionResponse();
        Optional<Promotion> promotionOpt = promotionRepository.findById(request.getId());
        if (promotionOpt.isPresent()) {
            Promotion promotion = promotionOpt.get();
            promotion.setDescription(request.getDescription()); // Giả sử có trường description
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

    // (Optional) nếu muốn giữ lại hàm này
    public List<PromotionResponse> getAllPromotions() {
        return searchPromotions(null);
    }
}