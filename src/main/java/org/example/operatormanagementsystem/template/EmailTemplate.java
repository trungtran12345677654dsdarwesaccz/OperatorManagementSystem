package org.example.operatormanagementsystem.template;

public enum EmailTemplate {
    VERIFICATION_CODE_EMAIL("[OperatorManagementSystem] Verify Code",
            "<div style=\"background-color: #f4f4f4; padding: 40px; text-align: center; font-family: 'Palatino Linotype', 'Book Antiqua', Palatino, serif;\">" +
                    "<div style=\"max-width: 600px; margin: auto; background: #ffffff; padding: 30px; border-radius: 8px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);\">" +
                    "<img src=\"https://dongvat.edu.vn/upload/2025/01/avatar-zenitsu-cute-10.webp\" alt=\"Zenitsu =))\" style=\"max-width: 100px\" />" +
                    "<h2 style=\"color: #333;\">Confirm your action</b></h2>" +
                    "<p style=\"color: #222; font-size: 16px;\">Your verification code is:</p>" +
                    "<div style=\"font-size: 32px; font-weight: bold; color: #333; padding: 20px; border: 2px solid #ddd; display: inline-block; background: #f9f9f9; margin: 20px 0;\">" +
                    "%s" +
                    "</div>" +
                    "<div style=\"text-align: left; margin-top: 20px; padding-top: 20px; border-top: 1px solid #ddd;\">" +
                    "<p style=\"color: #444; font-size: 14px;\">This code expires in <b>1 minutes</b>. Do not share this code with anyone.</p>" +
                    "<p style=\"color: #555; font-size: 12px;\">If you did not request this, you can safely ignore this email.</p>" +
                    "<p style=\"font-size: 12px; color: #666;\">Protected by <b>Group6</b></p>" +
                    "</div>" +
                    "</div>" +
                    "</div>");

    private final String subject;
    private final String body;

    EmailTemplate(String subject, String body) {
        this.subject = subject;
        this.body = body;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody(String code) {
        return String.format(body, code);
    }

    public static String buildTransportUnitApprovedEmail(String userName, String transportUnitName, String managerNote) {
        return "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<meta charset=\"UTF-8\">"
                + "<title>Transport Unit Approved</title>"
                // ... CSS tương tự như các email template khác ...
                + "</head>"
                + "<body>"
                + "<div class=\"container\">"
                + "<h1>Xin chào " + userName + ",</h1>"
                + "<p>Yêu cầu đăng ký đơn vị vận chuyển <b>" + transportUnitName + "</b> của bạn đã được <strong>CHẤP THUẬN</strong>!</p>"
                + "<p>Đơn vị vận chuyển của bạn hiện đã hoạt động trong hệ thống của chúng tôi.</p>"
                + (managerNote != null && !managerNote.isEmpty() ? "<p><strong>Ghi chú từ quản lý:</strong> " + managerNote + "</p>" : "")
                + "<p>Cảm ơn bạn đã hợp tác.</p>"
                + "<div class=\"footer\">"
                + "<p>&copy; 2025 Operator Management System. All rights reserved.</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
    }

    public static String buildTransportUnitRejectedEmail(String userName, String transportUnitName, String managerNote) {
        return "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<meta charset=\"UTF-8\">"
                + "<title>Transport Unit Rejected</title>"
                // ... CSS tương tự như các email template khác ...
                + "</head>"
                + "<body>"
                + "<div class=\"container\">"
                + "<h1>Xin chào " + userName + ",</h1>"
                + "<p>Yêu cầu đăng ký đơn vị vận chuyển <b>" + transportUnitName + "</b> của bạn đã bị <strong>TỪ CHỐI</strong>.</p>"
                + "<p><strong>Lý do:</strong> " + (managerNote != null && !managerNote.isEmpty() ? managerNote : "Không có ghi chú cụ thể từ quản lý.") + "</p>"
                + "<p>Vui lòng liên hệ với quản lý hoặc bộ phận hỗ trợ để biết thêm thông tin.</p>"
                + "<div class=\"footer\">"
                + "<p>&copy; 2025 Operator Management System. All rights reserved.</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
    }

    public static String buildGenericTransportUnitStatusUpdateEmail(String userName, String transportUnitName, String newStatus, String managerNote) {
        return "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<meta charset=\"UTF-8\">"
                + "<title>Transport Unit Status Update</title>"
                // ... CSS tương tự như các email template khác ...
                + "</head>"
                + "<body>"
                + "<div class=\"container\">"
                + "<h1>Xin chào " + userName + ",</h1>"
                + "<p>Trạng thái của đơn vị vận chuyển <b>" + transportUnitName + "</b> của bạn đã được cập nhật thành: <strong>" + newStatus + "</strong>.</p>"
                + (managerNote != null && !managerNote.isEmpty() ? "<p><strong>Ghi chú từ quản lý:</strong> " + managerNote + "</p>" : "")
                + "<p>Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với bộ phận hỗ trợ của chúng tôi.</p>"
                + "<div class=\"footer\">"
                + "<p>&copy; 2025 Operator Management System. All rights reserved.</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
    }

}
