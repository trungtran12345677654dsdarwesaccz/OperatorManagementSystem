package org.example.operatormanagementsystem.transportunit.maillistener;

import jakarta.mail.*;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.search.ComparisonTerm;
import jakarta.mail.search.FlagTerm; // <--- Import này đã có, sẽ sử dụng
import jakarta.mail.internet.InternetAddress; // Không dùng, có thể xóa nếu không cần
import jakarta.mail.search.AndTerm; // <--- Cần import này để kết hợp điều kiện tìm kiếm
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
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
    @Scheduled(fixedRate = 10000) // Chạy mỗi 10 giây (10000 ms) để debug nhanh hơn
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

        // properties.put("mail.debug", "true"); // Bật debug Javamail để xem chi tiết kết nối
        properties.put("mail.mime.charset", "UTF-8");
        properties.put("mail.imaps.partialfetch", "false"); // Tăng độ ổn định khi fetch toàn bộ email

        Session emailSession = Session.getDefaultInstance(properties);
        emailSession.setDebug(Boolean.parseBoolean(properties.getProperty("mail.debug", "false"))); // Đảm bảo debug được kích hoạt

        System.out.println("IMAP Host: " + imapHost);
        System.out.println("IMAP Port: " + imapPort);
        System.out.println("Mail Username: " + mailUsername);
        System.out.println("Mail Password (length): " + mailPassword.length()); // Kiểm tra độ dài mật khẩu ứng dụng (phải là 16)
        System.out.println("Onboarding API URL: " + onboardingApiUrl);
        System.out.println("Onboarding API Key (length): " + onboardingApiKey.length());

        Store store = null;
        Folder inbox = null;
        try {
            store = emailSession.getStore("imap");
            System.out.println("Attempting to connect to email store...");
            store.connect(imapHost, mailUsername, mailPassword);
            System.out.println("Successfully connected to email store.");

            inbox = store.getFolder("INBOX");
            // <--- RẤT QUAN TRỌNG: Mở folder ở chế độ READ_WRITE để có thể thay đổi flag
            inbox.open(Folder.READ_WRITE);
            System.out.println("Inbox opened. Message count: " + inbox.getMessageCount());

            LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
            Date dateTwentyFourHoursAgo = Date.from(twentyFourHoursAgo.atZone(ZoneId.systemDefault()).toInstant());
            SearchTerm receivedInLast24Hours = new ReceivedDateTerm(ComparisonTerm.GT, dateTwentyFourHoursAgo);

            // <--- THÊM ĐIỀU KIỆN TÌM KIẾM: CHỈ EMAIL CHƯA ĐỌC VÀ CÓ TIÊU ĐỀ PHÙ HỢP
            SearchTerm unreadEmails = new FlagTerm(new Flags(Flags.Flag.SEEN), false);

            // Tìm kiếm email chưa đọc trong 24 giờ qua VÀ có Subject phù hợp
            // SubjectSearchTerm (hoặc TextSearchTerm) không phân biệt chữ hoa chữ thường.
            // Để đơn giản, tôi sẽ kiểm tra subject sau khi lấy message, nhưng trong production
            // bạn có thể thêm SearchTerm cho subject vào đây để lọc ngay từ đầu.
            // Hiện tại, chúng ta sẽ lọc bằng code Java sau khi lấy message.
            SearchTerm combinedSearchTerm = new AndTerm(receivedInLast24Hours, unreadEmails);


            Message[] messages = inbox.search(combinedSearchTerm); // <--- Bây giờ chỉ tìm email chưa đọc

            System.out.println("Found " + messages.length + " unread emails in the last 24 hours matching criteria.");

            if (messages.length == 0) {
                System.out.println("No new relevant emails found. Double check subject line and email content.");
            }

            for (int i = 0; i < messages.length; i++) {
                Message message = messages[i];
                String subject = message.getSubject();
                String senderEmailFromHeader = "UNKNOWN";
                if (message.getFrom() != null && message.getFrom().length > 0) {
                    if (message.getFrom()[0] instanceof InternetAddress) {
                        senderEmailFromHeader = ((InternetAddress) message.getFrom()[0]).getAddress();
                    } else {
                        senderEmailFromHeader = message.getFrom()[0].toString();
                    }
                }

                Flags flags = message.getFlags();
                boolean isSeen = flags.contains(Flags.Flag.SEEN); // Luôn là false ở đây vì đã lọc unread

                System.out.println("\n--- Processing email [" + (i + 1) + "/" + messages.length + "] ---");
                System.out.println("Subject: " + subject);
                System.out.println("From (Parsed): " + senderEmailFromHeader);
                System.out.println("Received Date: " + message.getReceivedDate());
                System.out.println("Is Seen (before processing): " + isSeen); // Sẽ luôn là false

                // <--- LỌC SUBJECT TẠI ĐÂY (nếu không thêm SearchTerm cho subject ở trên)
                if (subject != null && subject.contains("[ĐĂNG KÝ ĐƠN VỊ VẬN CHUYỂN MỚI]")) {
                    System.out.println("Subject matches. Attempting to get content...");
                    String content = getTextFromMessage(message);
                    System.out.println("Nội dung email đọc được (RAW):\n" + content);

                    TransportUnitEmailRequest request = parseEmailContent(content, senderEmailFromHeader);

                    if (request != null) {
                        System.out.println("Đã parse thành công, gửi đến API...");
                        System.out.println("Parsed Request DTO: " + request.toString());
                        sendToOnboardingApi(request);
                        message.setFlag(Flags.Flag.SEEN, true); // Đánh dấu là đã đọc sau khi xử lý thành công
                        System.out.println("Đã gửi và đánh dấu email là ĐÃ ĐỌC.");
                    } else {
                        System.err.println("Không thể parse nội dung email hoặc dữ liệu không hợp lệ từ: " + subject);
                        System.err.println("Nội dung email bị lỗi parse:\n" + content);
                        message.setFlag(Flags.Flag.SEEN, true); // Đánh dấu là đã đọc để tránh xử lý lại email lỗi
                        System.out.println("Đã đánh dấu email lỗi parse là ĐÃ ĐỌC.");
                    }
                } else {
                    System.out.println("Subject does not match or is null. Marking as seen to prevent re-processing.");
                    message.setFlag(Flags.Flag.SEEN, true); // Đánh dấu là đã đọc để không xử lý lại email không liên quan
                    System.out.println("Đã đánh dấu email không liên quan là ĐÃ ĐỌC.");
                }
            }


        } catch (AuthenticationFailedException e) {
            System.err.println("LỖI XÁC THỰC: Username/Password/App Password sai hoặc bị chặn. Vui lòng kiểm tra lại application.properties và tài khoản email.");
            System.err.println("Chi tiết lỗi: " + e.getMessage());
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
        } finally {
            try {
                if (inbox != null && inbox.isOpen()) {
                    // <--- RẤT QUAN TRỌNG: close(true) để LƯU CÁC THAY ĐỔI VỀ FLAG
                    inbox.close(true);
                    System.out.println("Inbox closed and changes (like SEEN flag) saved.");
                }
                if (store != null && store.isConnected()) {
                    store.close();
                    System.out.println("Email store closed.");
                }
            } catch (MessagingException e) {
                System.err.println("Lỗi khi đóng kết nối email: " + e.getMessage());
            }
        }
    }

    private String getTextFromMessage(Message message) throws IOException, MessagingException {
        // ... (Giữ nguyên không thay đổi) ...
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
        // ... (Giữ nguyên không thay đổi) ...
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
        // ... (Giữ nguyên không thay đổi) ...
        return htmlString.replaceAll("<[^>]*>", "").replaceAll("&nbsp;", " ").trim();
    }

    /* =======================================================================
     *  Hàm parseEmailContent mới - linh hoạt & dễ bảo trì hơn
     * ===================================================================== */
    private TransportUnitEmailRequest parseEmailContent(String rawContent,
                                                        String defaultSenderEmail) {

        /* 1. Chuẩn hoá văn bản */
        String emailContent = rawContent
                .replaceAll("[\\u00A0\\u200B]", " ")   // xoá nbsp, zero-width…
                .replace("\r", "")                     // bỏ CR
                .replaceAll("[ \\t]{2,}", " ")         // gộp space
                .trim();

        final int FLAGS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.DOTALL;

        /* 2. Regex – BẮT BUỘC phải có [...]  */
        Pattern companyP = Pattern.compile(
                "Tên\\s*Công\\s*ty\\s*Vận\\s*(?:Tải|chuyển).*?\\[\\s*([^]]+?)\\s*]", FLAGS);

        Pattern contactP = Pattern.compile(
                "Tên\\s*Người\\s*Đại\\s*diện\\s*Liên\\s*hệ.*?\\[\\s*([^]]+?)\\s*]", FLAGS);

        Pattern phoneP   = Pattern.compile(
                "Số\\s*Điện\\s*Thoại\\s*Liên\\s*hệ.*?\\[\\s*([^]]+?)\\s*]", FLAGS);

        Pattern licenseP = Pattern.compile(
                "(?:Biển|Bằng)\\s*(?:Cấp)?\\s*Vận\\s*(?:Tải|Chuyển).*?\\[\\s*([^]]+?)\\s*]", FLAGS);

        Pattern noteP    = Pattern.compile(
                "Ghi\\s*Chú\\s*Thêm.*?\\[\\s*([^]]+?)\\s*]", FLAGS);

        /* email bất kỳ nằm trong ngoặc vuông/đơn */
        Pattern emailIn  = Pattern.compile(
                "[\\[(]([a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,6})[\\])]", FLAGS);

        /* 3. Mapping vào DTO */
        TransportUnitEmailRequest req = new TransportUnitEmailRequest();
        req.setNameCompany      (find(emailContent, companyP));
        req.setNamePersonContact(find(emailContent, contactP));
        req.setPhone            (find(emailContent, phoneP));
        req.setLicensePlate     (find(emailContent, licenseP));
        req.setNote( Optional.ofNullable(find(emailContent, noteP)).orElse("") );

        String sender = find(emailContent, emailIn);
        if (sender == null || sender.isBlank()) sender = defaultSenderEmail;
        if (sender == null || sender.isBlank()) sender = mailUsername;      // fallback cuối
        req.setSenderEmail(sender);

        /* 4. Debug log */
        System.out.printf("""
        Parsed – Company : '%s'
                 Contact : '%s'
                 Phone   : '%s'
                 Plate   : '%s'
                 Note    : '%s'
                 Email   : '%s'%n""",
                req.getNameCompany(), req.getNamePersonContact(), req.getPhone(),
                req.getLicensePlate(), req.getNote(), req.getSenderEmail());

        /* 5. Kiểm tra field bắt buộc */
        if (Stream.of(req.getNameCompany(), req.getNamePersonContact(),
                        req.getPhone(), req.getLicensePlate(), req.getSenderEmail())
                .anyMatch(s -> s == null || s.isBlank())) {

            System.err.println("❌ Parse error – thiếu field bắt buộc, bỏ qua email.");
            return null;
        }
        return req;
    }

    /* Helper: trả về group(1) nếu match, null nếu không */
    private static String find(String src, Pattern p) {
        Matcher m = p.matcher(src);
        return m.find() ? m.group(1).trim() : null;
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
//da co cai loc thong tin nhan va xac thuc 2 yeu to
//loc thong tin khi nhan
// chua co ma hoa, va chua co su dung ma hoa khi nhan email
// xong sơm làm thêm cái chưa có cx đc
// 1 SO cach de dam bao an toan ma hoa mail khi nhan , nma van co kha nang bi hack khi nguoi dung gui thong tin cua ho di trc khi den web antoan

//1.Xac minh so huu email (gui email xac nhan voi link/token)
//Tom tat lai: Day la bien phap de dam bao nguoi gui email thuc su so huu dia chi email do. Thay vi xu ly ngay, he thong se gui mot email xac nhan voi mot link duy nhat cho nguoi gui. Chi khi nguoi gui nhap vao link nay, yeu cau moi duoc xu ly tiep. Dieu nay giup chong lai viec gia mao nguoi gui va spam.
//
//2. Ap dung gioi han toc do (Rate Limiting) / chong spam cho luong xu ly email
//Tom tat lai: Bien phap nay giup ngan chan ke tan cong gui qua nhieu email lien tuc den he thong, gay qua tai dich vu lang nghe email hoac API dich. Bang cach gioi han so luong email duoc xu ly tu mot dia chi trong mot khoang thoi gian nhat dinh, hoac gioi han so luong email xu ly moi lan chay, he thong se tro nen on dinh hon truoc cac cuoc tan cong DoS qua email.
//
//3. Bao mat API Key bang bien moi truong hoac Secret Manager
//Tom tat lai: Day la cach bao ve cac khoa API quan trong. Thay vi luu truc tiep API key trong cac file cau hinh cua ung dung (ma co the bi lo neu file bi danh cap), ban nen luu no duoi dang bien moi truong tren server hoac su dung mot Dich vu Quan ly Bi mat (Secret Manager). Dieu nay giup ngan chan ke tan cong lay duoc API key va su dung no de truy cap trai phep cac API cua ban.