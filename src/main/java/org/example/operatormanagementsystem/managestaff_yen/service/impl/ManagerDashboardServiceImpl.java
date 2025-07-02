package org.example.operatormanagementsystem.managestaff_yen.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.entity.Booking;
import org.example.operatormanagementsystem.managestaff_yen.dto.response.ManagerDashboardResponse;
import org.example.operatormanagementsystem.managestaff_yen.repository.OperatorStaffRepository;
import org.example.operatormanagementsystem.managestaff_yen.repository.ManagerBookingRepository;
import org.example.operatormanagementsystem.managestaff_yen.repository.ManagerFeedbackToStaffRepository;
import org.example.operatormanagementsystem.managestaff_yen.service.ManagerDashboardService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ManagerDashboardServiceImpl implements ManagerDashboardService {

    private final ManagerBookingRepository bookingRepository;
    private final OperatorStaffRepository operatorStaffRepository;
    private final ManagerFeedbackToStaffRepository feedbackRepository;

    @Override
    public ManagerDashboardResponse getDashboardData(Integer managerId) {
        LocalDate today = LocalDate.now();

        long totalToday = bookingRepository.countByManagerAndCreatedDate(managerId, today);
        long successToday = bookingRepository.countByManagerAndCreatedDateAndStatus(managerId, today, "SUCCESS");
        long ongoing = bookingRepository.countByManagerAndStatus(managerId, "IN_PROGRESS");
        long failed = bookingRepository.countByManagerAndStatus(managerId, "FAILED");
//        long onlineOperators = operatorStaffRepository.countByManagerAndOnlineTrue(managerId);
        long revenueToday = bookingRepository.sumRevenueByManagerAndCreatedDate(managerId, today) != null
                ? bookingRepository.sumRevenueByManagerAndCreatedDate(managerId, today)
                : 0;

        return ManagerDashboardResponse.builder()
                .totalOrdersToday(totalToday)
                .successfulOrders(successToday)
                .ongoingOrders(ongoing)
                .failedOrders(failed)
//                .onlineOperators(onlineOperators)
                .totalRevenueToday(revenueToday)
                .monthlyPerformance(getMonthlyPerformance(managerId))
                .topOperators(getTopOperators(managerId))
                .recentIssues(getRecentIssues(managerId))
                .build();
    }

    private List<ManagerDashboardResponse.MonthlyPerformance> getMonthlyPerformance(Integer managerId) {
        return IntStream.rangeClosed(0, 3)
                .mapToObj(i -> YearMonth.now().minusMonths(3 - i))
                .map(ym -> ManagerDashboardResponse.MonthlyPerformance.builder()
                        .monthLabel("T" + ym.getMonthValue() + "/" + ym.getYear())
                        .successfulOrders(bookingRepository.countByManagerAndMonthAndStatus(
                                managerId, ym.getYear(), ym.getMonthValue(), "SUCCESS"))
                        .failedOrders(bookingRepository.countByManagerAndMonthAndStatus(
                                managerId, ym.getYear(), ym.getMonthValue(), "FAILED"))
                        .build()
                ).collect(Collectors.toList());
    }

    private List<ManagerDashboardResponse.TopOperator> getTopOperators(Integer managerId) {
        return operatorStaffRepository.findTop5ByManagerOrderByBookingCountDesc(managerId).stream()
                .map(os -> {
                    // Lấy điểm trung bình của operator từ feedbackRepository
                    Double averageRating = feedbackRepository.getAverageRatingForOperator(os.getOperatorId());

                    // Kiểm tra nếu averageRating là null và gán giá trị mặc định
                    if (averageRating == null) {
                        averageRating = 0.0; // Giá trị mặc định khi không có kết quả
                    }

                    // Trả về TopOperator với các thông tin
                    return ManagerDashboardResponse.TopOperator.builder()
                            .operatorId(os.getOperatorId())
                            .fullName(os.getUsers().getFullName())
                            .totalOrders(os.getBookings().size())
                            .averageRating(averageRating) // Sử dụng giá trị mặc định nếu cần
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<ManagerDashboardResponse.IssueOrderInfo> getRecentIssues(Integer managerId) {
        return bookingRepository.findRecentIssuesByManager(managerId).stream()
                .map(b -> ManagerDashboardResponse.IssueOrderInfo.builder()
                        .orderId(b.getBookingId())
                        .customerName(b.getCustomer().getUsers().getFullName())
                        .status(b.getStatus())
                        .note(b.getNote())
                        .createdAt(b.getCreatedAt().toString())
                        .build()
                ).collect(Collectors.toList());
    }

}
