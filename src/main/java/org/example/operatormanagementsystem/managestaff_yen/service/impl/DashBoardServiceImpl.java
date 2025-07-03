package org.example.operatormanagementsystem.managestaff_yen.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.entity.Payment;
import org.example.operatormanagementsystem.managestaff_yen.dto.request.ChartFilterRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.request.TopOperatorFilterRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.response.*;
import org.example.operatormanagementsystem.managestaff_yen.repository.DashboardRepo.BookingDashboardRepository;
import org.example.operatormanagementsystem.managestaff_yen.repository.DashboardRepo.IssueLogRepository;
import org.example.operatormanagementsystem.managestaff_yen.repository.DashboardRepo.PaymentDashboardRepository;
import org.example.operatormanagementsystem.managestaff_yen.repository.PromotionRepository;
import org.example.operatormanagementsystem.managestaff_yen.service.DashboardService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashBoardServiceImpl implements DashboardService {

    private final BookingDashboardRepository bookingRepository;
    private final PaymentDashboardRepository paymentRepository;
    private final PromotionRepository promotionRepository;
    private final IssueLogRepository issueLogRepository;

    @Override
    public DashboardOverviewResponse getOverview() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        long totalOrders = bookingRepository.countByCreatedAtBetween(startOfDay, endOfDay);

        BigDecimal revenueToday = paymentRepository
                .findByPaidDate(today)
                .stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long activePromotions = promotionRepository.countByStatus("ACTIVE");

        long totalDelivered = bookingRepository.countByCreatedAtBetween(startOfDay, endOfDay);
        long onTimeDelivered = bookingRepository.countByCreatedAtBetweenAndStatus(startOfDay, endOfDay, "ONTIME");
        double onTimeRate = totalDelivered == 0 ? 0 : (onTimeDelivered * 100.0 / totalDelivered);

        return new DashboardOverviewResponse(totalOrders, revenueToday, activePromotions, onTimeRate);
    }

    @Override
    public List<RecentIssueResponse> getRecentIssues(int limit) {
        return issueLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit))
                .stream()
                .map(i -> new RecentIssueResponse(
                        i.getIssueId(),
                        i.getDescription(),
                        i.getStatus(),
                        i.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<TopOperatorResponse> getTopOperators(TopOperatorFilterRequest request) {
        List<Object[]> rawData = bookingRepository.getTopOperatorsStats();
        return rawData.stream()
                .limit(request.getLimit())
                .map(obj -> new TopOperatorResponse(
                        (Integer) obj[0],
                        (String) obj[1],
                        ((Number) obj[2]).intValue(),
                        ((Number) obj[3]).doubleValue()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<ChartDataPointResponse> getChartData(ChartFilterRequest request) {
        LocalDate today = LocalDate.now();
        LocalDate start;

        switch (request.getRange().toLowerCase()) {
            case "week" -> start = today.with(DayOfWeek.MONDAY);
            case "month" -> start = today.withDayOfMonth(1);
            default -> start = today;
        }

        if ("orders".equalsIgnoreCase(request.getType())) {
            return bookingRepository.countByDateBetween(start, today)
                    .stream()
                    .map(obj -> new ChartDataPointResponse((LocalDate) obj[0], ((Number) obj[1]).intValue()))
                    .collect(Collectors.toList());
        } else if ("revenue".equalsIgnoreCase(request.getType())) {
            return paymentRepository.sumAmountByPaidDateBetween(start, today)
                    .stream()
                    .map(obj -> new ChartDataPointResponse((LocalDate) obj[0], ((BigDecimal) obj[1]).intValue()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
