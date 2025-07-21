package org.example.operatormanagementsystem.ManageHungBranch.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.search.ComparisonTerm;
import jakarta.mail.search.FlagTerm; // <--- Import này đã có, sẽ sử dụng
import jakarta.mail.search.AndTerm; // <--- Cần import này để kết hợp điều kiện tìm kiếm
import jakarta.mail.search.ReceivedDateTerm;
import jakarta.mail.search.SearchTerm;
import org.example.operatormanagementsystem.ManageHungBranch.dto.request.StorageUnitEmailRequest;
import org.example.operatormanagementsystem.transportunit.dto.request.TransportUnitEmailRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class StorageUnitMailListenerService {

    @Value("${mail.imap.host}")
    private String imapHost;
    @Value("${mail.imap.port}")
    private String imapPort;
    @Value("${mail.username}")
    private String mailUsername;
    @Value("${mail.password}")
    private String mailPassword;
    @Value("${app.onboarding.api-key.storage-unit}")
    private String onboardingApiKey;
    @Value("${app.onboarding.api-url.storage-unit}")
    private String onboardingApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // Đọc email mỗi phút (demo, có thể đổi lại)
    @Scheduled(fixedRate = 60000)
    public void checkEmailsAndOnboardStorage() {
        Properties properties = new Properties();
        properties.put("mail.imap.host", imapHost);
        properties.put("mail.imap.port", imapPort);
        properties.put("mail.imap.ssl.enable", "true");
        properties.put("mail.imap.auth", "true");
        Session emailSession = Session.getDefaultInstance(properties);
        Store store = null;
        Folder inbox = null;

        try {
            store = emailSession.getStore("imap");
            store.connect(imapHost, mailUsername, mailPassword);
            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            // Chỉ lọc email chưa đọc, subject đúng
            System.out.println("Đang kiểm tra mail chưa đọc...");
            Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            System.out.println("Tìm thấy " + messages.length + " mail chưa đọc.");
            for (Message message : messages) {
                System.out.println("Subject mail: " + message.getSubject());
                if (message.getSubject() == null || !message.getSubject().contains("[ĐĂNG KÝ KHO MỚI]")) {
                    message.setFlag(Flags.Flag.SEEN, true);
                    continue;
                }
                String content = getTextFromMessage(message);
                System.out.println("Nội dung mail: " + content);
                StorageUnitEmailRequest req = parseEmailContent(content, extractSender(message));
                System.out.println("Đã parse thành DTO: " + req);
                sendToOnboardingApi(req);
                message.setFlag(Flags.Flag.SEEN, true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (inbox != null && inbox.isOpen()) inbox.close(true); } catch (Exception ignore) {}
            try { if (store != null && store.isConnected()) store.close(); } catch (Exception ignore) {}
        }
    }

    private String extractSender(Message message) throws MessagingException {
        Address[] froms = message.getFrom();
        return froms == null ? null : ((InternetAddress) froms[0]).getAddress();
    }

    // Parse text theo mẫu mail bạn quy định
    private StorageUnitEmailRequest parseEmailContent(String rawContent, String defaultSenderEmail) {
        String emailContent = rawContent
                .replaceAll("[\\u00A0\\u200B]", " ")
                .replace("\r", "")
                .replaceAll("[ \\t]{2,}", " ")
                .trim();

        final int FLAGS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.DOTALL;

        Pattern nameP      = Pattern.compile("Tên\\s*Kho.*?\\[\\s*([^]]+?)\\s*]", FLAGS);
        Pattern addressP   = Pattern.compile("Địa\\s*chỉ.*?\\[\\s*([^]]+?)\\s*]", FLAGS);
        Pattern phoneP     = Pattern.compile("Số\\s*điện\\s*thoại.*?\\[\\s*([^]]+?)\\s*]", FLAGS);
        Pattern noteP      = Pattern.compile("Ghi\\s*chú.*?\\[\\s*([^]]*?)\\s*]", FLAGS);
        Pattern imageP     = Pattern.compile("Link\\s*ảnh.*?\\[\\s*([^]]*?)\\s*]", FLAGS);
        Pattern slotCountP = Pattern.compile("Số\\s*lượng\\s*slot.*?\\[\\s*(\\d+)\\s*]", FLAGS);

        String name = find(emailContent, nameP);
        String address = find(emailContent, addressP);
        String phone = find(emailContent, phoneP);
        String note = find(emailContent, noteP);
        String imageUrl = find(emailContent, imageP);
        Integer slotCount = null;

        try {
            String slotStr = find(emailContent, slotCountP);
            if (slotStr != null && !slotStr.isEmpty()) {
                slotCount = Integer.parseInt(slotStr);
            }
        } catch (NumberFormatException ignored) {}

        return StorageUnitEmailRequest.builder()
                .name(name)
                .address(address)
                .phone(phone)
                .note(note)
                .imageUrl(imageUrl)
                .slotCount(slotCount)
                .senderEmail(defaultSenderEmail)
                .build();
    }
    private static String find(String src, Pattern p) {
        Matcher m = p.matcher(src);
        return m.find() ? m.group(1).trim() : null;
    }

    private String getTextFromMessage(Message message) throws Exception {
        if (message.isMimeType("text/plain")) return message.getContent().toString();
        if (message.isMimeType("text/html")) return message.getContent().toString();
        if (message.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) message.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) return bp.getContent().toString();
            }
        }
        return "";
    }

    private void sendToOnboardingApi(StorageUnitEmailRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-KEY", onboardingApiKey);

        HttpEntity<StorageUnitEmailRequest> entity = new HttpEntity<>(request, headers);

        try {
            restTemplate.postForEntity(onboardingApiUrl, entity, String.class);
            System.out.println("Sent storage unit data to API successfully.");
        } catch (Exception e) {
            System.err.println("Error sending storage unit data to API webhook: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void sendSuggestionEmailToSender(String to) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(mailUsername, mailPassword);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailUsername));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject("Mẫu đăng ký kho");

            String body = """
                    Vui lòng điền đúng mẫu sau đây và gửi lại:
                    --------------------------------------------
                    [ĐĂNG KÝ KHO MỚI]
                    --------------------------------------------
                    Tên Kho: [Tên kho]
                    Địa chỉ: [Địa chỉ]
                    Số điện thoại: [Số điện thoại]
                    Email liên hệ: [Email liên hệ]
                    Số lượng slot: [Số lượng slot]
                    Ghi chú: [Tùy chọn]
                    --------------------------------------------
                    Hãy điền thông tin trong dấu ngoặc vuông nhé.
                    """;

            message.setText(body);
            Transport.send(message);
            System.out.println("Sent registration form email to sender: " + to);
        } catch (Exception e) {
            System.err.println("Failed to send registration form email: " + e.getMessage());
        }
    }
}