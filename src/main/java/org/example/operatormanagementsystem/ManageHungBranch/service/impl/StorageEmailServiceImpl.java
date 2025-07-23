    package org.example.operatormanagementsystem.ManageHungBranch.service.impl;

    import lombok.RequiredArgsConstructor;
    import org.example.operatormanagementsystem.ManageHungBranch.service.StorageEmailService;
    import org.example.operatormanagementsystem.enumeration.ApprovalStatus;
    import org.springframework.mail.javamail.JavaMailSender;
    import org.springframework.mail.javamail.MimeMessageHelper;
    import org.springframework.scheduling.annotation.Async;
    import org.springframework.stereotype.Service;
    import jakarta.mail.MessagingException;
    import jakarta.mail.internet.MimeMessage;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;

    @Service
    @RequiredArgsConstructor
    public class StorageEmailServiceImpl implements StorageEmailService {

        private final JavaMailSender mailSender;
        private static final Logger logger = LoggerFactory.getLogger(StorageEmailServiceImpl.class);
        private static final String SYSTEM_EMAIL = "tranduytrung251105@gmail.com";

        @Override
        @Async
        public void sendStorageUnitApprovalNotification(
                String recipientEmail,
                String recipientName,
                ApprovalStatus status,
                String managerNote) {

            if (recipientEmail == null || recipientEmail.isEmpty()) {
                logger.error("Recipient email is null or empty, cannot send notification");
                return;
            }

            String subject;
            String htmlBody;

            if (status == ApprovalStatus.APPROVED) {
                subject = "[OperatorManagementSystem] Chúc mừng! Yêu cầu đăng ký kho của bạn đã được duyệt";
                htmlBody = "<html>" +
                        "<body style=\"font-family: Arial, sans-serif;\">" +
                        "<h2 style=\"color: #2e6da4;\">Chúc mừng " + recipientEmail + "!</h2>" +
                        "<p>Yêu cầu đăng ký kho của bạn đã được quản lý phê duyệt thành công.</p>" +
                        "<p>Kho của bạn hiện đã được kích hoạt trên hệ thống.</p>" +
                        "<p><strong>Ghi chú từ quản lý:</strong> " +
                        (managerNote != null && !managerNote.isEmpty() ? managerNote : "Không có") + "</p>" +
                        "<br>" +
                        "<p>Trân trọng,<br>Hệ thống Quản lý Vận Chuyển</p>" +
                        "</body>" +
                        "</html>";
            } else if (status == ApprovalStatus.REJECTED) {
                subject = "[OperatorManagementSystem] Thông báo: Yêu cầu đăng ký kho của bạn đã bị từ chối";
                htmlBody = "<html>" +
                        "<body style=\"font-family: Arial, sans-serif;\">" +
                        "<h2 style=\"color: #d9534f;\">Xin chào " + recipientName + "!</h2>" +
                        "<p>Chúng tôi rất tiếc phải thông báo rằng yêu cầu đăng ký kho của bạn đã không được phê duyệt.</p>" +
                        "<p><strong>Lý do từ chối:</strong> " +
                        (managerNote != null && !managerNote.isEmpty() ? managerNote : "Không có lý do cụ thể.") + "</p>" +
                        "<p>Vui lòng kiểm tra lại thông tin và gửi lại yêu cầu nếu cần thiết.</p>" +
                        "<br>" +
                        "<p>Trân trọng,<br>Hệ thống Quản lý Vận hành</p>" +
                        "</body>" +
                        "</html>";
            } else {
                logger.warn("Invalid approval status: {}", status);
                return;
            }

            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setTo(recipientEmail);
                helper.setSubject(subject);
                helper.setText(htmlBody, true); // HTML content
                helper.setFrom(SYSTEM_EMAIL);

                mailSender.send(message);
                logger.info("Sent approval notification email to: {}", recipientEmail);
            } catch (MessagingException e) {
                logger.error("Failed to send email to {}: {}", recipientEmail, e.getMessage(), e);
            }
        }
    }
