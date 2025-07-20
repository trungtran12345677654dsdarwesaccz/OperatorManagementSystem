package org.example.operatormanagementsystem.customer_thai.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.customer_thai.dto.response.PromotionBookingResponse;
import org.example.operatormanagementsystem.customer_thai.repository.PromotionRepository;
import org.example.operatormanagementsystem.customer_thai.service.PromotionService;
import org.example.operatormanagementsystem.entity.Promotion;
import org.example.operatormanagementsystem.enumeration.PromotionStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service("promotionService_thai")
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    @Qualifier("promotionRepository_thai")
    private final PromotionRepository promotionRepository;

    @Override
    public List<PromotionBookingResponse> getActivePromotions() {
        List<Promotion> promotions = promotionRepository.findByStatus(PromotionStatus.ACTIVE);
        return promotions.stream()
                .map(this::mapToPromotionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PromotionBookingResponse getPromotionByName(String name) {
        return promotionRepository.findByName(name)
                .map(this::mapToPromotionResponse)
                .orElse(null);
    }

    private PromotionBookingResponse mapToPromotionResponse(Promotion promotion) {
        return PromotionBookingResponse.builder()
                .id(promotion.getId())
                .name(promotion.getName())
                .description(promotion.getDescription())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .status(promotion.getStatus())
                .build();
    }
}