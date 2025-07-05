package org.example.operatormanagementsystem.customer_thai.service;

import org.example.operatormanagementsystem.customer_thai.dto.response.PromotionBookingResponse;

import java.util.List;

public interface PromotionService {
    /**
     * Gets all active promotions
     * @return List of active promotions
     */
    List<PromotionBookingResponse> getActivePromotions();
    
    /**
     * Gets promotion by name
     * @param name The name of the promotion
     * @return The promotion details or null if not found
     */
    PromotionBookingResponse getPromotionByName(String name);
} 