package org.example.operatormanagementsystem.managestaff_yen.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.entity.IssueLog;
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
import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
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

        long totalOrders = bookingRepository.countByDate(today);

        BigDecimal revenueToday = paymentRepository
                .findByPaidDate(today)
                .stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long activePromotions = promotionRepository.countByStatus("ACTIVE");

        long totalDelivered = bookingRepository.countByDate(today);
        long onTimeDelivered = bookingRepository.countByDateAndStatus(today, "ONTIME");
        double onTimeRate = totalDelivered == 0 ? 0 : (onTimeDelivered * 100.0 / totalDelivered);

        return new DashboardOverviewResponse(totalOrders, revenueToday, activePromotions, onTimeRate);
    }

    @Override
    public List<ChartDataPointResponse> getOrderChartData(ChartFilterRequest request) {
        LocalDate start;
        LocalDate end;
        LocalDate today = LocalDate.now();

        switch (request.getRange().toLowerCase()) {
            case "week" -> {
                start = today.with(DayOfWeek.MONDAY);
                end = today;
            }
            case "month" -> {
                start = today.withDayOfMonth(1);
                end = today;
            }
            case "year" -> {
                start = today.withDayOfYear(1);
                end = today;
            }
            case "range" -> {
                start = request.getFromDate();
                end = request.getToDate();
            }
            default -> {
                start = today;
                end = today;
            }
        }

        return bookingRepository.countByDateBetween(start, end)
                .stream()
                .map(obj -> new ChartDataPointResponse(
                        ((Date) obj[0]).toLocalDate(),
                        ((Number) obj[1]).intValue()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<ChartDataPointResponse> getRevenueChartData(ChartFilterRequest request) {
        LocalDate start;
        LocalDate end;
        LocalDate today = LocalDate.now();

        switch (request.getRange().toLowerCase()) {
            case "week" -> {
                start = today.with(DayOfWeek.MONDAY);
                end = today;
            }
            case "month" -> {
                start = today.withDayOfMonth(1);
                end = today;
            }
            case "year" -> {
                start = today.withDayOfYear(1);
                end = today;
            }
            case "range" -> {
                start = request.getFromDate();
                end = request.getToDate();
            }
            default -> {
                start = today;
                end = today;
            }
        }

        return paymentRepository.sumAmountByPaidDateBetween(start, end)
                .stream()
                .map(obj -> new ChartDataPointResponse(
                        ((Date) obj[0]).toLocalDate(),
                        ((BigDecimal) obj[1]).intValue()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<TopOperatorResponse> getTopOperators(TopOperatorFilterRequest request) {
        LocalDate start = request.getFromDate();
        LocalDate end = request.getToDate();
        List<Object[]> rawData = bookingRepository.getTopOperatorsStatsByDate(start, end);
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
    public List<RecentIssueResponse> getRecentIssues(LocalDate fromDate, LocalDate toDate, int limit) {
        List<IssueLog> issues = issueLogRepository.findRecentIssuesNative(fromDate, toDate, limit);

        return issues.stream()
                .map(issue -> new RecentIssueResponse(
                        issue.getIssueId(),
                        issue.getDescription(),
                        issue.getStatus(),
                        issue.getCreatedAt()
                ))
                .collect(Collectors.toList());

    }

    }
