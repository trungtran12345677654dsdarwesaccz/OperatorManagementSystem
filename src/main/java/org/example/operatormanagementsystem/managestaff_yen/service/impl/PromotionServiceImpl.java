package org.example.operatormanagementsystem.managestaff_yen.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.entity.Promotion;
import org.example.operatormanagementsystem.managestaff_yen.dto.request.AddPromotionRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.request.CancelPromotionRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.request.UpdatePromotionRequest;
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
        Promotion promotion = new Promotion();
        promotion.setName(request.getName());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setStatus(request.getStatus() != null ? request.getStatus() : "active");
        promotion = promotionRepository.save(promotion);

        return PromotionResponse.builder()
                .id(promotion.getId())
                .name(promotion.getName())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .status("success")
                .message("Promotion added successfully")
                .build();
    }

    @Override
    public PromotionResponse updatePromotion(UpdatePromotionRequest request) {
        Optional<Promotion> opt = promotionRepository.findById(request.getId());
        if (opt.isPresent()) {
            Promotion promotion = opt.get();
            promotion.setName(request.getName());
            if (request.getStatus() != null) {
                promotion.setStatus(request.getStatus());
            }
            promotionRepository.save(promotion);
            return PromotionResponse.builder()
                    .id(promotion.getId())
                    .status("success")
                    .message("Promotion updated successfully")
                    .build();
        }
        return PromotionResponse.builder()
                .status("error")
                .message("Promotion not found")
                .build();
    }

    @Override
    public PromotionResponse updatePromotionDates(UpdatePromotionRequest request) {
        Optional<Promotion> opt = promotionRepository.findById(request.getId());
        if (opt.isPresent()) {
            Promotion promotion = opt.get();
            promotion.setStartDate(request.getStartDate());
            promotion.setEndDate(request.getEndDate());
            promotionRepository.save(promotion);
            return PromotionResponse.builder()
                    .id(promotion.getId())
                    .status("success")
                    .message("Promotion dates updated successfully")
                    .build();
        }
        return PromotionResponse.builder()
                .status("error")
                .message("Promotion not found")
                .build();
    }

    @Override
    public PromotionResponse updateDescription(UpdatePromotionRequest request) {
        Optional<Promotion> opt = promotionRepository.findById(request.getId());
        if (opt.isPresent()) {
            Promotion promotion = opt.get();
            promotion.setDescription(request.getDescription());
            promotionRepository.save(promotion);
            return PromotionResponse.builder()
                    .id(promotion.getId())
                    .status("success")
                    .message("Promotion description updated successfully")
                    .build();
        }
        return PromotionResponse.builder()
                .status("error")
                .message("Promotion not found")
                .build();
    }

    @Override
    public PromotionResponse cancelPromotion(CancelPromotionRequest request) {
        Optional<Promotion> opt = promotionRepository.findById(request.getId());
        if (opt.isPresent()) {
            Promotion promotion = opt.get();
            promotion.setStatus("cancelled");
            promotionRepository.save(promotion);
            return PromotionResponse.builder()
                    .id(promotion.getId())
                    .status("success")
                    .message("Promotion cancelled successfully")
                    .build();
        }
        return PromotionResponse.builder()
                .status("error")
                .message("Promotion not found")
                .build();
    }

    @Override
    public List<PromotionResponse> searchPromotions(String keyword, String status) {
        return promotionRepository.searchByKeywordAndStatus(keyword, status).stream()
                .map(p -> PromotionResponse.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .startDate(p.getStartDate())
                        .endDate(p.getEndDate())
                        .status(p.getStatus())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<PromotionResponse> getAllPromotions() {
        return searchPromotions(null, null);
    }

    @Override
    public PromotionStatisticsResponse getPromotionOverview() {
        long total = promotionRepository.count();
        long active = promotionRepository.countByStatus("ACTIVE");
        long upcoming = promotionRepository.countByStatus("UPCOMING");
        long expired = promotionRepository.countByStatus("EXPIRED");

        long bookingCount = bookingRepository.countByPromotionIsNotNull();
        Double revenue = bookingRepository.sumTotalByPromotionNotNull();
        long feedbackCount = feedbackRepository.countPositiveFeedbackWithPromotion();

        return new PromotionStatisticsResponse(
                total,
                active,
                upcoming,
                expired,
                bookingCount,
                revenue != null ? revenue : 0,
                feedbackCount
        );
    }

    @Override
    public List<ChartDataPointResponse> getPromotionRevenue(String rangeType, LocalDate from, LocalDate to) {
        String pattern = mapRangeTypeToPattern(rangeType);
        List<Object[]> results = bookingRepository.sumPromotionRevenue(pattern, from, to);
        List<ChartDataPointResponse> data = new ArrayList<>();

        for (Object[] row : results) {
            String rawDate = (String) row[0];
            LocalDate date;

            try {
                switch (rangeType) {
                    case "day" -> {
                        date = LocalDate.parse(rawDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    }
                    case "month" -> {
                        String[] parts = rawDate.split("-");
                        int year = Integer.parseInt(parts[0]);
                        int month = Integer.parseInt(parts[1]);
                        date = LocalDate.of(year, month, 1);
                    }
                    case "year" -> {
                        int year = Integer.parseInt(rawDate);
                        date = LocalDate.of(year, 1, 1);
                    }
                    default -> throw new IllegalArgumentException("Invalid rangeType: " + rangeType);
                }

                Long value = row[1] != null ? ((Number) row[1]).longValue() : 0L;
                data.add(new ChartDataPointResponse(date, value.intValue()));
            } catch (Exception e) {
                System.err.println(">>> Error parsing date string: " + rawDate + " for rangeType: " + rangeType);
                e.printStackTrace();
            }
        }

        return data;
    }


    @Override
    public List<ChartDataPointResponse> getPromotionBookingCount(String rangeType, LocalDate from, LocalDate to) {
        String pattern = mapRangeTypeToPattern(rangeType);
        List<Object[]> results = bookingRepository.countBookingsWithPromotion(pattern, from, to);
        List<ChartDataPointResponse> data = new ArrayList<>();

        for (Object[] row : results) {
            String rawDate = row[0] != null ? row[0].toString() : null;
            if (rawDate == null) continue;

            LocalDate date;
            try {
                switch (rangeType) {
                    case "day" -> date = LocalDate.parse(rawDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    case "month" -> {
                        String[] parts = rawDate.split("-");
                        int year = Integer.parseInt(parts[0]);
                        int month = Integer.parseInt(parts[1]);
                        date = LocalDate.of(year, month, 1);
                    }
                    case "year" -> {
                        int year = Integer.parseInt(rawDate);
                        date = LocalDate.of(year, 1, 1);
                    }
                    default -> throw new IllegalArgumentException("Invalid rangeType: " + rangeType);
                }
                Long count = row[1] != null ? ((Number) row[1]).longValue() : 0L;
                data.add(new ChartDataPointResponse(date, count.intValue()));
            } catch (Exception e) {
                System.err.printf("Loi parse date: %s, rangeType: %s%n", rawDate, rangeType);
            }
        }

        return data;
    }


    @Override
    public List<PieChartSegmentResponse> getPromotionStatusRatio() {
        List<PieChartSegmentResponse> list = new ArrayList<>();
        list.add(new PieChartSegmentResponse("ACTIVE", promotionRepository.countByStatus("ACTIVE")));
        list.add(new PieChartSegmentResponse("UPCOMING", promotionRepository.countByStatus("UPCOMING")));
        list.add(new PieChartSegmentResponse("EXPIRED", promotionRepository.countByStatus("EXPIRED")));
        return list;
    }

    @Override
    public List<BarChartDataResponse> getPositiveFeedbackByPromotion() {
        List<Object[]> result = feedbackRepository.countPositiveFeedbackGroupedByPromotion();
        List<BarChartDataResponse> data = new ArrayList<>();
        for (Object[] row : result) {
            String name = (String) row[0];
            Long count = (Long) row[1];
            data.add(new BarChartDataResponse(name, count.intValue()));
        }
        return data;
    }

}
