package org.example.operatormanagementsystem.transportunit.maillistener;


import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage; // Dòng này không dùng, nhưng vẫn được cung cấp lại từ bạn
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.search.AndTerm;
import jakarta.mail.search.ComparisonTerm;
import jakarta.mail.search.FlagTerm;
import jakarta.mail.search.ReceivedDateTerm;
import jakarta.mail.search.SearchTerm;
import org.example.operatormanagementsystem.transportunit.dto.request.TransportUnitEmailRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EmailListenerService {

    @Value("${mail.imap.host}")
    private String imapHost;
    @Value("${mail.imap.port}")
    private String imapPort;
    @Value("${mail.username}")
    private String mailUsername;
    @Value("${mail.password}")
    private String mailPassword;
    @Value("${app.onboarding.api-url}") // URL của API webhook của bạn
    private String onboardingApiUrl;
    @Value("${app.onboarding.api-key}")
    private String onboardingApiKey;

    // RestTemplate để gửi HTTP request đến API của bạn
    private final RestTemplate restTemplate = new RestTemplate();

    // KÍCH HOẠT HÀM NÀY CHẠY ĐỊNH KỲ
//    @Scheduled(fixedRate = 10000) // Chạy mỗi 10 giây (10000 ms) để debug nhanh hơn
    public void scheduleEmailCheck() {
        System.out.println("--- SCHEDULED TASK: Checking emails at " + System.currentTimeMillis() + " ---");
        checkEmailsAndOnboard();
        System.out.println("--- SCHEDULED TASK: Email check finished ---");
    }


    public void checkEmailsAndOnboard() {
        Properties properties = new Properties();
        properties.put("mail.imap.host", imapHost);
        properties.put("mail.imap.port", imapPort);
        properties.put("mail.imap.ssl.enable", "true");
        properties.put("mail.imap.auth", "true");

        properties.put("mail.mime.charset", "UTF-8");
        properties.put("mail.imaps.partialfetch", "false");

        properties.put("mail.debug", System.getProperty("mail.debug", "false"));

        Session emailSession = Session.getDefaultInstance(properties);
        emailSession.setDebug(Boolean.parseBoolean(properties.getProperty("mail.debug")));

        System.out.println("IMAP Host: " + imapHost);
        System.out.println("IMAP Port: " + imapPort);
        System.out.println("Mail Username: " + mailUsername);
        System.out.println("Mail Password (length): " + mailPassword.length());
        System.out.println("Onboarding API URL: " + onboardingApiUrl);
        System.out.println("Onboarding API Key (length): " + onboardingApiKey.length());

        try {
            Store store = emailSession.getStore("imap");
            System.out.println("Attempting to connect to email store...");
            store.connect(imapHost, mailUsername, mailPassword);
            System.out.println("Successfully connected to email store.");

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);
            System.out.println("Inbox opened. Message count: " + inbox.getMessageCount());

            SearchTerm unreadEmails = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
            LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
            Date dateTwentyFourHoursAgo = Date.from(twentyFourHoursAgo.atZone(ZoneId.systemDefault()).toInstant());
            SearchTerm receivedInLast24Hours = new ReceivedDateTerm(ComparisonTerm.GT, dateTwentyFourHoursAgo);

            SearchTerm combinedSearchTerm = new AndTerm(unreadEmails, receivedInLast24Hours);

            Message[] messages = inbox.search(combinedSearchTerm);

            System.out.println("Found " + messages.length + " unread emails in the last 24 hours.");

            for (int i = 0; i < messages.length; i++) {
                Message message = messages[i];
                String subject = message.getSubject();

                System.out.println("Processing email with subject: " + subject);

                if (subject != null && subject.contains("[ĐĂNG KÝ ĐƠN VỊ VẬN CHUYỂN MỚI]")) {
                    System.out.println("Subject matches. Attempting to get content...");
                    String content = getTextFromMessage(message);
                    System.out.println("Nội dung email đọc được (RAW):\n" + content);

                    TransportUnitEmailRequest request = parseEmailContent(content);

                    if (request != null) {
                        System.out.println("Đã parse thành công, gửi đến API...");
                        System.out.println("Parsed Request DTO: " + request.toString());
                        sendToOnboardingApi(request);
                        message.setFlag(Flags.Flag.SEEN, true);
                        System.out.println("Đã gửi và đánh dấu email.");
                    } else {
                        System.err.println("Không thể parse nội dung email hoặc dữ liệu không hợp lệ từ: " + subject);
                        System.err.println("Nội dung email bị lỗi parse:\n" + content);
                        message.setFlag(Flags.Flag.SEEN, true);
                    }
                } else {
                    System.out.println("Subject does not match or is null. Marking as seen.");
                    message.setFlag(Flags.Flag.SEEN, true);
                }
            }

            inbox.close(false);
            store.close();
            System.out.println("Email check cycle completed.");

        } catch (AuthenticationFailedException e) {
            System.err.println("LỖI XÁC THỰC: Username/Password/App Password sai hoặc bị chặn. Vui lòng kiểm tra lại application.properties và tài khoản email.");
            e.printStackTrace();
        } catch (MessagingException e) {
            System.err.println("LỖI KẾT NỐI EMAIL (IMAP/POP3): " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("LỖI I/O khi đọc nội dung email: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("LỖI KHÔNG XÁC ĐỊNH trong quá trình xử lý email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getTextFromMessage(Message message) throws IOException, MessagingException {
        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        } else if (message.isMimeType("text/html")) {
            return removeHtmlTags(message.getContent().toString());
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            return getTextFromMimeMultipart(mimeMultipart);
        }
        return "";
    }

    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        StringBuilder result = new StringBuilder();
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result.append(bodyPart.getContent().toString());
            } else if (bodyPart.isMimeType("text/html")) {
                result.append(removeHtmlTags(bodyPart.getContent().toString()));
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
            }
        }
        return result.toString();
    }

    private String removeHtmlTags(String htmlString) {
        return htmlString.replaceAll("<[^>]*>", "");
    }

    private TransportUnitEmailRequest parseEmailContent(String emailContent) {
        // CẬP NHẬT REGEX ĐỂ KHỚP VỚI ĐỊNH DẠNG EMAIL MỚI
        // (bao gồm dấu hoa thị '*' và linh hoạt với khoảng trắng/xuống dòng)
        Pattern companyPattern = Pattern.compile("\\*Tên Công ty Vận chuyển:\\*\\s*\\[(.*?)\\]", Pattern.DOTALL);
        Pattern contactPersonPattern = Pattern.compile("\\*Tên Người Đại diện Liên hệ:\\*\\s*\\[(.*?)\\]", Pattern.DOTALL);
        Pattern phonePattern = Pattern.compile("\\*Số Điện Thoại Liên hệ:\\*\\s*\\[(.*?)\\]", Pattern.DOTALL);
        Pattern licensePlatePattern = Pattern.compile("\\*Bằng Cấp Vận Chuyển \\(ví dụ ABC\\):\\*\\s*\\[(.*?)\\]", Pattern.DOTALL);
        Pattern notePattern = Pattern.compile("\\*Ghi Chú Thêm \\(nếu có\\):\\*\\s*\\[(.*?)\\]", Pattern.DOTALL);


        TransportUnitEmailRequest request = new TransportUnitEmailRequest();

        Matcher companyMatcher = companyPattern.matcher(emailContent);
        if (companyMatcher.find()) request.setNameCompany(companyMatcher.group(1).trim());

        Matcher contactPersonMatcher = contactPersonPattern.matcher(emailContent);
        if (contactPersonMatcher.find()) request.setNamePersonContact(contactPersonMatcher.group(1).trim());

        Matcher phoneMatcher = phonePattern.matcher(emailContent);
        if (phoneMatcher.find()) request.setPhone(phoneMatcher.group(1).trim());

        Matcher licensePlateMatcher = licensePlatePattern.matcher(emailContent);
        if (licensePlateMatcher.find()) request.setLicensePlate(licensePlateMatcher.group(1).trim());

        Matcher noteMatcher = notePattern.matcher(emailContent);
        if (noteMatcher.find()) request.setNote(noteMatcher.group(1).trim());
        else request.setNote("");

        System.out.println("Parsed - Company: '" + request.getNameCompany() + "'");
        System.out.println("Parsed - Contact Person: '" + request.getNamePersonContact() + "'");
        System.out.println("Parsed - Phone: '" + request.getPhone() + "'");
        System.out.println("Parsed - License Plate: '" + request.getLicensePlate() + "'");
        System.out.println("Parsed - Note: '" + request.getNote() + "'");

        if (request.getNameCompany() == null || request.getNameCompany().isEmpty() ||
                request.getNamePersonContact() == null || request.getNamePersonContact().isEmpty() ||
                request.getPhone() == null || request.getPhone().isEmpty() ||
                request.getLicensePlate() == null || request.getLicensePlate().isEmpty()) {
            System.err.println("Dữ liệu email bị thiếu hoặc rỗng các trường bắt buộc sau khi parse.");
            return null;
        }

        return request;
    }

    private void sendToOnboardingApi(TransportUnitEmailRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-KEY", onboardingApiKey);

        HttpEntity<TransportUnitEmailRequest> entity = new HttpEntity<>(request, headers);

        try {
            restTemplate.postForEntity(onboardingApiUrl, entity, String.class);
            System.out.println("Đã gửi dữ liệu đơn vị vận chuyển đến API thành công.");
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi dữ liệu đến API webhook: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
