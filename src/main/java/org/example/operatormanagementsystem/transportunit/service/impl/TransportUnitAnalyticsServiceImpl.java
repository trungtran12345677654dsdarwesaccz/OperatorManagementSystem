package org.example.operatormanagementsystem.transportunit.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.entity.TransportUnitApproval;
import org.example.operatormanagementsystem.enumeration.ApprovalStatus;
import org.example.operatormanagementsystem.enumeration.UserStatus;
import org.example.operatormanagementsystem.transportunit.dto.response.*;
import org.example.operatormanagementsystem.transportunit.repository.ManagerRepository;
import org.example.operatormanagementsystem.transportunit.repository.TransportUnitApprovalRepository;
import org.example.operatormanagementsystem.transportunit.repository.TransportUnitRepository;
import org.example.operatormanagementsystem.transportunit.service.TransportUnitAnalyticsService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransportUnitAnalyticsServiceImpl implements TransportUnitAnalyticsService {

    private final TransportUnitRepository transportUnitRepository;
    private final TransportUnitApprovalRepository transportUnitApprovalRepository;
    private final ManagerRepository managerRepository;

    @Override
    public DashboardStatsResponse getDashboardStats() {
        int totalUnits = transportUnitRepository.countAll();
        int pending = transportUnitRepository.countByStatus(UserStatus.PENDING_APPROVAL);
        int active = transportUnitRepository.countByStatus(UserStatus.ACTIVE);
        int inactive = transportUnitRepository.countByStatus(UserStatus.INACTIVE);
        int todayApprovals = transportUnitApprovalRepository.countTodayByStatus(ApprovalStatus.APPROVED);
        int todayRejections = transportUnitApprovalRepository.countTodayByStatus(ApprovalStatus.REJECTED);

        double approvalRate = transportUnitApprovalRepository.countApprovalRate();
        double avgTime = transportUnitApprovalRepository.calculateAvgProcessingTime();

        return new DashboardStatsResponse(
                totalUnits, pending, active, inactive,
                approvalRate, avgTime, todayApprovals, todayRejections
        );
    }

    @Override
    public List<HistoricalDataResponse> getHistoricalData(LocalDate startDate, LocalDate endDate, String groupBy) {
        return switch (groupBy.toUpperCase()) {
            case "WEEK" -> transportUnitRepository.getHistoricalDataWeekly(startDate, endDate);
            case "MONTH" -> transportUnitRepository.getHistoricalDataMonthly(startDate, endDate);
            default -> throw new IllegalArgumentException("Invalid groupBy value: " + groupBy);
        };
    }

    @Override
    public List<WeeklyActivityResponse> getWeeklyActivity() {
        return transportUnitApprovalRepository.getWeeklyActivityStats();
    }

    @Override
    public List<ManagerPerformanceResponse> getManagerPerformance(LocalDate startDate, LocalDate endDate) {
        return transportUnitApprovalRepository.getManagerPerformanceBetween(startDate, endDate);
    }

    @Override
    public List<StatusDistributionResponse> getStatusDistribution() {
        List<Object[]> result = transportUnitRepository.getStatusCounts();
        long total = result.stream()
                .mapToLong(r -> (Long) r[1])
                .sum();

        return result.stream()
                .map(r -> {
                    UserStatus status = (UserStatus) r[0];
                    long count = (Long) r[1];
                    double percentage = total == 0 ? 0 : (count * 100.0) / total;
                    return new StatusDistributionResponse(status, count, percentage);
                })
                .collect(Collectors.toList());
    }


    @Override
    public List<ApprovalTrendResponse> getApprovalTrends(int days) {
        LocalDate fromDate = LocalDate.now().minusDays(days);
        return transportUnitApprovalRepository.getApprovalTrendsSince(fromDate);
    }

    @Override
    public PerformanceMetricsResponse getPerformanceMetrics() {
        List<TransportUnitApproval> processed = transportUnitApprovalRepository.findProcessedApprovals(
                ApprovalStatus.APPROVED, ApprovalStatus.REJECTED
        );

        long totalApproved = processed.stream().filter(a -> a.getStatus() == ApprovalStatus.APPROVED).count();
        long totalRejected = processed.stream().filter(a -> a.getStatus() == ApprovalStatus.REJECTED).count();

        double avgApprovalTime = processed.stream()
                .filter(a -> a.getStatus() == ApprovalStatus.APPROVED)
                .filter(a -> a.getRequestedAt() != null && a.getProcessedAt() != null)
                .mapToDouble(a -> Duration.between(a.getRequestedAt(), a.getProcessedAt()).toMinutes() / 60.0)
                .average().orElse(0);

        double avgRejectionTime = processed.stream()
                .filter(a -> a.getStatus() == ApprovalStatus.REJECTED)
                .filter(a -> a.getRequestedAt() != null && a.getProcessedAt() != null)
                .mapToDouble(a -> Duration.between(a.getRequestedAt(), a.getProcessedAt()).toMinutes() / 60.0)
                .average().orElse(0);

        long bottleneckCount = processed.stream()
                .filter(a -> a.getRequestedAt() != null && a.getProcessedAt() != null)
                .filter(a -> Duration.between(a.getRequestedAt(), a.getProcessedAt()).toHours() > 48)
                .count();

        // Thống kê lý do từ chối phổ biến
        Map<String, Long> rejectionReasons = processed.stream()
                .filter(a -> a.getStatus() == ApprovalStatus.REJECTED)
                .map(TransportUnitApproval::getManagerNote)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(reason -> reason.trim().toLowerCase(), Collectors.counting()));

        List<String> topRejectionReasons = rejectionReasons.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Only count valid records for system efficiency
        long validCount = processed.stream()
                .filter(a -> a.getRequestedAt() != null && a.getProcessedAt() != null)
                .count();
        double systemEfficiency = 0.0;
        if (validCount > 0) {
            systemEfficiency = 100.0 * processed.stream()
                    .filter(a -> a.getRequestedAt() != null && a.getProcessedAt() != null)
                    .filter(a -> Duration.between(a.getRequestedAt(), a.getProcessedAt()).toHours() <= 24)
                    .count() / (double) validCount;
        }

        return new PerformanceMetricsResponse(
                avgApprovalTime,
                avgRejectionTime,
                (int) bottleneckCount,
                topRejectionReasons,
                systemEfficiency
        );
    }

}
