package org.example.operatormanagementsystem.managestaff_yen.controller;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.managestaff_yen.dto.response.StaffPerformanceResponse;
import org.example.operatormanagementsystem.managestaff_yen.service.EmailReportService;
import org.example.operatormanagementsystem.managestaff_yen.service.StaffPerformanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/report-performance")
@RequiredArgsConstructor
public class StaffPerformanceController {

    private final StaffPerformanceService staffPerformanceService;
    private final EmailReportService emailReportService;

    @GetMapping("/report")
    public ResponseEntity<List<StaffPerformanceResponse>> getPerformanceReport() {
        List<StaffPerformanceResponse> responses = staffPerformanceService.calculateAll();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/send-emails")
    public ResponseEntity<Void> sendEmails() {
        staffPerformanceService.sendPerformanceEmails();
        return ResponseEntity.ok().build();
    }

   // ✅ Gửi email theo danh sách được chọn từ FE
    @PostMapping("/send-selected-emails")
   public ResponseEntity<Void> sendSelectedEmails(@RequestBody List<StaffPerformanceResponse> selectedStaffs)
          throws MessagingException {

        for (StaffPerformanceResponse staff : selectedStaffs) {
           if ("EXCELLENT".equalsIgnoreCase(staff.getPerformanceLevel())) {
            emailReportService.sendPerformancePraiseEmail(
                       staff.getEmail(), staff.getFullName(), staff.getPerformanceScore());
            } else if ("POOR".equalsIgnoreCase(staff.getPerformanceLevel())) {
               emailReportService.sendWarningEmail(staff);
          }
     }

        return ResponseEntity.ok().build();
    }
}
