package org.example.operatormanagementsystem.managestaff_yen.service.impl;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.managestaff_yen.dto.response.StaffPerformanceResponse;
import org.example.operatormanagementsystem.managestaff_yen.repository.DashboardRepo.BookingDashboardRepository;
import org.example.operatormanagementsystem.managestaff_yen.repository.DashboardRepo.IssueLogRepository;
import org.example.operatormanagementsystem.managestaff_yen.repository.FeedbackPromotionRepository;
import org.example.operatormanagementsystem.managestaff_yen.repository.OperatorStaffRepository;
import org.example.operatormanagementsystem.managestaff_yen.service.EmailReportService;
import org.example.operatormanagementsystem.managestaff_yen.service.StaffPerformanceService;
import org.example.operatormanagementsystem.repository.LoginHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffPerformanceServiceImpl implements StaffPerformanceService {

    private final BookingDashboardRepository bookingRepo;
    private final FeedbackPromotionRepository feedbackRepo;
    private final LoginHistoryRepository loginRepo;
    private final IssueLogRepository issueRepo;
    private final OperatorStaffRepository operatorRepo;
    private final EmailReportService emailService;

    @Override
    public List<StaffPerformanceResponse> calculateAll() {
        LocalDateTime start = YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime end = YearMonth.now().atEndOfMonth().atTime(23, 59, 59);

        return operatorRepo.findAll().stream()
                .map(staff -> {
                    int bookings = bookingRepo.countByOperatorStaffAndCreatedAtBetween(staff, start, end);
                    int feedbacks = feedbackRepo.countByOperatorStaffAndStarGreaterThanEqualAndCreatedAtBetween(staff, 4, start, end);
                    int logins = loginRepo.countByUserAndLoginTimeBetween(staff.getUsers(), start, end);
                    int issues = issueRepo.countByBooking_OperatorStaffAndCreatedAtBetween(staff, start, end);

                    int score = bookings * 4 + feedbacks * 3 + logins * 2 - issues * 5;
                    String level = score >= 80 ? "EXCELLENT" : score < 50 ? "POOR" : "AVERAGE";

                    return StaffPerformanceResponse.builder()
                            .operatorId(staff.getOperatorId())
                            .fullName(staff.getUsers().getFullName())
                            .email(staff.getUsers().getEmail())
                            .bookingCount(bookings)
                            .goodFeedbackCount(feedbacks)
                            .loginCount(logins)
                            .issueCount(issues)
                            .performanceScore(score)
                            .performanceLevel(level)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public void sendPerformanceEmails() {
        calculateAll().forEach(response -> {
            try {
                if ("EXCELLENT".equals(response.getPerformanceLevel())) {
                    emailService.sendPerformancePraiseEmail(
                            response.getEmail(),
                            response.getFullName(),
                            response.getPerformanceScore()
                    );
                } else if ("POOR".equals(response.getPerformanceLevel())) {
                    emailService.sendWarningEmail(response);
                }
            } catch (MessagingException e) {
                System.err.println("Email sending failed for: " + response.getFullName() + " - " + e.getMessage());
            }
        });
    }
}
