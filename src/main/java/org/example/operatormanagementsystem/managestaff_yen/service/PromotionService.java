
package org.example.operatormanagementsystem.managestaff_yen.service;

import org.example.operatormanagementsystem.enumeration.DiscountType;
import org.example.operatormanagementsystem.enumeration.PromotionStatus;
import org.example.operatormanagementsystem.managestaff_yen.dto.request.UpdatePromotionRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.request.CancelPromotionRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.request.AddPromotionRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.response.*;


import java.time.LocalDate;
import java.util.List;

public interface PromotionService {
    PromotionResponse updatePromotionDates(UpdatePromotionRequest request);
    PromotionResponse updatePromotion(UpdatePromotionRequest request);
    PromotionResponse cancelPromotion(CancelPromotionRequest request);

    List<PromotionResponse> searchPromotions(String keyword, PromotionStatus status, DiscountType discountType, Double discountValue);

    default List<PromotionResponse> searchPromotions(String keyword) {
        return searchPromotions(keyword, null, null, null);
    }

    PromotionResponse addPromotion(AddPromotionRequest request);
    PromotionResponse updateDescription(UpdatePromotionRequest request);
    List<PromotionResponse> getAllPromotions();

    PromotionStatisticsResponse getPromotionOverview();
    List<ChartDataPointResponse> getPromotionRevenue(String rangeType, LocalDate from, LocalDate to);
    List<ChartDataPointResponse> getPromotionBookingCount(String rangeType, LocalDate from, LocalDate to);
    List<PieChartSegmentResponse> getPromotionStatusRatio();
    List<BarChartDataResponse> getPositiveFeedbackByPromotion();
}