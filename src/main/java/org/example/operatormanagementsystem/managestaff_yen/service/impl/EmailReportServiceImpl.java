package org.example.operatormanagementsystem.managestaff_yen.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.managestaff_yen.dto.response.StaffPerformanceResponse;
import org.example.operatormanagementsystem.managestaff_yen.service.EmailReportService;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailReportServiceImpl implements EmailReportService {

    private final JavaMailSender mailSender;

    @Async
    @Override
    public void sendPerformancePraiseEmail(String recipientEmail, String fullName, int performanceScore) throws MessagingException {
        String subject = "[OperatorManagementSystem] Chúc mừng hiệu suất xuất sắc!";
        String htmlBody = "<html>" +
                "<body style=\"font-family: Arial, sans-serif;\">" +
                "<h2 style=\"color: #2e6da4;\">Chúc mừng " + fullName + "!</h2>" +
                "<p>Bạn là một trong những nhân viên có hiệu suất làm việc <strong>xuất sắc</strong> trong tháng này.</p>" +
                "<p><strong>Điểm hiệu suất:</strong> " + performanceScore + "</p>" +
                "<p>Chúng tôi rất trân trọng sự nỗ lực và cam kết của bạn.</p>" +
                "<br>" +
                "<p>Trân trọng,<br>Ban quản lý</p>" +
                "</body>" +
                "</html>";

        sendHtmlEmail(recipientEmail, subject, htmlBody);
    }

    @Async
    @Override
    public void sendWarningEmail(String recipientEmail, String fullName, int performanceScore) throws MessagingException {
            String subject = "[OperatorManagementSystem] Cảnh báo về hiệu suất làm việc";
            String htmlBody = "<html>" +
                    "<body style=\"font-family: Arial, sans-serif;\">" +
                    "<h2 style=\"color: #d9534f;\">Xin chào " + fullName + "!</h2>" +
                    "<p>Hiệu suất làm việc của bạn trong tháng này <strong>chưa đạt yêu cầu</strong>.</p>" +
                    "<p><strong>Điểm hiệu suất:</strong> " + performanceScore + "</p>" +
                    "<p>Chúng tôi mong rằng bạn sẽ cải thiện chất lượng công việc trong thời gian tới để đạt được kết quả tốt hơn.</p>" +
                    "<p>Nếu cần hỗ trợ hoặc phản hồi, hãy liên hệ với quản lý trực tiếp.</p>" +
                    "<br>" +
                    "<p>Trân trọng,<br>Ban quản lý</p>" +
                    "</body>" +
                    "</html>";

            sendHtmlEmail(recipientEmail, subject, htmlBody);
        }



        private void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        helper.setFrom("no-reply@operatormanagement.com");

        mailSender.send(message);
    }
}
