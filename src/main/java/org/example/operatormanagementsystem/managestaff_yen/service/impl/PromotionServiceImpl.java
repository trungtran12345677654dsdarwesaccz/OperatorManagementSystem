package org.example.operatormanagementsystem.managestaff_yen.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.entity.Promotion;
import org.example.operatormanagementsystem.enumeration.DiscountType;
import org.example.operatormanagementsystem.enumeration.PromotionStatus;
import org.example.operatormanagementsystem.managestaff_yen.dto.request.*;
import org.example.operatormanagementsystem.managestaff_yen.dto.response.*;
import org.example.operatormanagementsystem.managestaff_yen.repository.BookingPromotionRepository;
import org.example.operatormanagementsystem.managestaff_yen.repository.FeedbackPromotionRepository;
import org.example.operatormanagementsystem.managestaff_yen.repository.PromotionRepository;
import org.example.operatormanagementsystem.managestaff_yen.service.PromotionService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final BookingPromotionRepository bookingRepository;
    private final FeedbackPromotionRepository feedbackRepository;

    private String mapRangeTypeToPattern(String rangeType) {
        return switch (rangeType) {
            case "day" -> "%Y-%m-%d";
            case "month" -> "%Y-%m";
            case "year" -> "%Y";
            default -> throw new IllegalArgumentException("Invalid rangeType: " + rangeType);
        };
    }

    @Override
    public PromotionResponse addPromotion(AddPromotionRequest request) {
        Promotion promotion = Promotion.builder()
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(Optional.ofNullable(request.getStatus()).orElse(PromotionStatus.ACTIVE))
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .description(request.getDescription())
                .build();

        promotion = promotionRepository.save(promotion);

        return PromotionResponse.builder()
                .id(promotion.getId())
                .name(promotion.getName())
                .description(promotion.getDescription())
                .discountType(promotion.getDiscountType())
                .discountValue(promotion.getDiscountValue())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .status(PromotionStatus.ACTIVE)
                .message("Promotion added successfully")
                .build();
    }

    @Override
    public PromotionResponse updatePromotion(UpdatePromotionRequest request) {
        return promotionRepository.findById(request.getId())
                .map(promotion -> {
                    promotion.setName(request.getName());
                    promotion.setDescription(request.getDescription());
                    promotion.setStartDate(request.getStartDate());
                    promotion.setEndDate(request.getEndDate());

                    if (request.getStatus() != null) promotion.setStatus(request.getStatus());
                    if (request.getDiscountType() != null) promotion.setDiscountType(request.getDiscountType());
                    if (request.getDiscountValue() != null) promotion.setDiscountValue(request.getDiscountValue());

                    promotionRepository.save(promotion);
                    return PromotionResponse.builder()
                            .id(promotion.getId())
                            .status(PromotionStatus.ACTIVE)
                            .message("Promotion updated successfully")
                            .build();
                })
                .orElse(PromotionResponse.builder()
                        .status(PromotionStatus.PENDING) // Sử dụng PENDING cho trường hợp không tìm thấy
                        .message("Promotion not found")
                        .build());
    }

    @Override
    public PromotionResponse updatePromotionDates(UpdatePromotionRequest request) {
        return promotionRepository.findById(request.getId())
                .map(promotion -> {
                    promotion.setStartDate(request.getStartDate());
                    promotion.setEndDate(request.getEndDate());
                    promotionRepository.save(promotion);
                    return PromotionResponse.builder()
                            .id(promotion.getId())
                            .status(PromotionStatus.ACTIVE)
                            .message("Promotion dates updated successfully")
                            .build();
                })
                .orElse(PromotionResponse.builder()
                        .status(PromotionStatus.PENDING) // Sử dụng PENDING cho trường hợp không tìm thấy
                        .message("Promotion not found")
                        .build());
    }

    @Override
    public PromotionResponse updateDescription(UpdatePromotionRequest request) {
        return promotionRepository.findById(request.getId())
                .map(promotion -> {
                    promotion.setDescription(request.getDescription());
                    promotionRepository.save(promotion);
                    return PromotionResponse.builder()
                            .id(promotion.getId())
                            .status(PromotionStatus.ACTIVE)
                            .message("Promotion description updated successfully")
                            .build();
                })
                .orElse(PromotionResponse.builder()
                        .status(PromotionStatus.PENDING) // Sử dụng PENDING cho trường hợp không tìm thấy
                        .message("Promotion not found")
                        .build());
    }

    @Override
    public PromotionResponse cancelPromotion(CancelPromotionRequest request) {
        return promotionRepository.findById(request.getId())
                .map(promotion -> {
                    promotion.setStatus(PromotionStatus.CANCELED);
                    promotionRepository.save(promotion);
                    return PromotionResponse.builder()
                            .id(promotion.getId())
                            .status(PromotionStatus.CANCELED)
                            .message("Promotion cancelled successfully")
                            .build();
                })
                .orElse(PromotionResponse.builder()
                        .status(PromotionStatus.PENDING) // Sử dụng PENDING cho trường hợp không tìm thấy
                        .message("Promotion not found")
                        .build());
    }

    @Override
    public List<PromotionResponse> searchPromotions(String keyword, PromotionStatus status, DiscountType discountType, Double discountValue) {
        return promotionRepository.searchByKeywordAndStatus(keyword, status, discountType, discountValue)
                .stream()
                .map(p -> PromotionResponse.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .description(p.getDescription())
                        .discountType(p.getDiscountType())
                        .discountValue(p.getDiscountValue())
                        .startDate(p.getStartDate())
                        .endDate(p.getEndDate())
                        .status(p.getStatus())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<PromotionResponse> getAllPromotions() {
        return searchPromotions(null, null, null, null);
    }

    @Override
    public PromotionStatisticsResponse getPromotionOverview() {
        return new PromotionStatisticsResponse(
                promotionRepository.count(),
                promotionRepository.countByStatus(PromotionStatus.ACTIVE),
                promotionRepository.countByStatus(PromotionStatus.UPCOMING),
                promotionRepository.countByStatus(PromotionStatus.EXPIRED),
                promotionRepository.countByStatus(PromotionStatus.CANCELED),
                promotionRepository.countByStatus(PromotionStatus.PENDING),
                bookingRepository.countByPromotionIsNotNull(),
                Optional.ofNullable(bookingRepository.sumTotalByPromotionNotNull()).orElse(0.0),
                feedbackRepository.countPositiveFeedbackWithPromotion()
        );
    }

    @Override
    public List<ChartDataPointResponse> getPromotionRevenue(String rangeType, LocalDate from, LocalDate to) {
        String pattern = mapRangeTypeToPattern(rangeType);
        List<Object[]> results = bookingRepository.sumPromotionRevenue(pattern, from, to);
        return parseChartData(results, rangeType);
    }

    @Override
    public List<ChartDataPointResponse> getPromotionBookingCount(String rangeType, LocalDate from, LocalDate to) {
        String pattern = mapRangeTypeToPattern(rangeType);
        List<Object[]> results = bookingRepository.countBookingsWithPromotion(pattern, from, to);
        return parseChartData(results, rangeType);
    }

    private List<ChartDataPointResponse> parseChartData(List<Object[]> results, String rangeType) {
        List<ChartDataPointResponse> data = new ArrayList<>();
        for (Object[] row : results) {
            try {
                String rawDate = row[0] != null ? row[0].toString() : null;
                if (rawDate == null) continue;

                LocalDate date = switch (rangeType) {
                    case "day" -> LocalDate.parse(rawDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    case "month" -> {
                        String[] parts = rawDate.split("-");
                        yield LocalDate.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), 1);
                    }
                    case "year" -> LocalDate.of(Integer.parseInt(rawDate), 1, 1);
                    default -> throw new IllegalArgumentException("Invalid rangeType: " + rangeType);
                };

                int value = row[1] != null ? ((Number) row[1]).intValue() : 0;
                data.add(new ChartDataPointResponse(date, value));
            } catch (Exception e) {
                System.err.printf("Lỗi parse dữ liệu: %s (%s)%n", row[0], rangeType);
            }
        }
        return data;
    }

    @Override
    public List<PieChartSegmentResponse> getPromotionStatusRatio() {
        return Arrays.stream(PromotionStatus.values())
                .map(status -> new PieChartSegmentResponse(status.name(), promotionRepository.countByStatus(status)))
                .collect(Collectors.toList());
    }

    @Override
    public List<BarChartDataResponse> getPositiveFeedbackByPromotion() {
        return feedbackRepository.countPositiveFeedbackGroupedByPromotion()
                .stream()
                .map(row -> new BarChartDataResponse((String) row[0], ((Number) row[1]).intValue()))
                .collect(Collectors.toList());
    }
}