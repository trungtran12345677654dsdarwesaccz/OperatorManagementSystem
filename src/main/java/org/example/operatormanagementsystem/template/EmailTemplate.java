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
}
