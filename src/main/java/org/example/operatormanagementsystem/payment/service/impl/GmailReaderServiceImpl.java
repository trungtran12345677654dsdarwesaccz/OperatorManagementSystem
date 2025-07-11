package org.example.operatormanagementsystem.payment.service.impl;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.payment.dto.SmsMessageDto;
import org.example.operatormanagementsystem.payment.service.GmailReaderService;
import org.example.operatormanagementsystem.payment.service.PaymentService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class GmailReaderServiceImpl implements GmailReaderService {

    private final Gmail gmailService;
    private final PaymentService paymentService;

    @Override
    public void readLatestSmsEmails() {
        try {
            var messages = gmailService.users().messages().list("me")
                    .setQ("subject:'SMS Banking'")
                    .setMaxResults(3L)
                    .execute();

            for (Message msg : messages.getMessages()) {
                var fullMsg = gmailService.users().messages().get("me", msg.getId()).execute();
                var content = extractBody(fullMsg);

                // Extract timestamp từ Gmail message
                long internalDateMillis = fullMsg.getInternalDate();
                String timestamp = Instant.ofEpochMilli(internalDateMillis)
                        .atOffset(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ISO_INSTANT); // ISO format

                // Trích xuất thông tin từ nội dung email
                BigDecimal amount = extractAmount(content);
                String note = extractNote(content);

                System.out.println("==> Extracted amount: " + amount);
                System.out.println("==> Extracted note: " + note);
                System.out.println("==> Timestamp: " + timestamp);

                // Gửi về service
                SmsMessageDto sms = new SmsMessageDto("SMS_GATEWAY", content, timestamp);
                paymentService.confirmPaymentFromSms(sms, null); // gọi mà không có request
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String extractBody(Message message) {
        var part = message.getPayload();
        var body = part.getBody().getData();

        if (body == null && part.getParts() != null) {
            for (var sub : part.getParts()) {
                if ("text/plain".equals(sub.getMimeType())) {
                    body = sub.getBody().getData();
                    break;
                }
            }
        }

        return body != null ? new String(Base64.getDecoder().decode(body)) : "";
    }

    private BigDecimal extractAmount(String msg) {
        Matcher m = Pattern.compile("\\+(\\d+(?:,\\d{3})*)").matcher(msg);
        return m.find() ? new BigDecimal(m.group(1).replace(",", "")) : BigDecimal.ZERO;
    }

    private String extractNote(String msg) {
        Matcher m = Pattern.compile("(BOOKING\\s?\\d+)").matcher(msg.toUpperCase());
        return m.find() ? m.group(1).replace(" ", "").trim() : "";
    }
}
