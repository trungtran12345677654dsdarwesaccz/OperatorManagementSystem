package org.example.operatormanagementsystem.transportunit.maillistener;
import jakarta.mail.internet.MimeMessage;
import org.example.operatormanagementsystem.config.CloudinaryService;
import org.example.operatormanagementsystem.enumeration.TransportAvailabilityStatus;

import jakarta.mail.*;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.search.ComparisonTerm;
import jakarta.mail.search.FlagTerm; // <--- Import này đã có, sẽ sử dụng
import jakarta.mail.internet.InternetAddress; // Không dùng, có thể xóa nếu không cần
import jakarta.mail.search.AndTerm; // <--- Cần import này để kết hợp điều kiện tìm kiếm
import jakarta.mail.search.ReceivedDateTerm;
import jakarta.mail.search.SearchTerm;
import org.example.operatormanagementsystem.transportunit.dto.request.TransportUnitEmailRequest;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Base64;
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
    @Autowired
    private CloudinaryService cloudinaryService;


    // RestTemplate để gửi HTTP request đến API của bạn
    private final RestTemplate restTemplate = new RestTemplate();

    // KÍCH HOẠT HÀM NÀY CHẠY ĐỊNH KỲ
    @Scheduled(fixedRate = 20000) // Chạy mỗi 10 giây (10000 ms) để debug nhanh hơn
    public void scheduleEmailCheck() {
        System.out.println("--- SCHEDULED TASK: Checking emails at " + System.currentTimeMillis() + " ---");
        checkEmailsAndOnboard();
        System.out.println("--- SCHEDULED TASK: Email check finished ---");
    }
    private String extractSender(Message message) throws MessagingException {
        if (message.getFrom() != null && message.getFrom().length > 0) {
            Address address = message.getFrom()[0];
            if (address instanceof InternetAddress) {
                return ((InternetAddress) address).getAddress();
            } else {
                return address.toString();
            }
        }
        return "UNKNOWN";
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
        if (htmlString == null || htmlString.isBlank()) return "";

        // 1. Loại bỏ tất cả thẻ HTML như <p>, <b>, <br>, ...
        String noHtml = htmlString.replaceAll("<[^>]*>", "");

        // 2. Thay thế các ký tự đặc biệt HTML (ví dụ &nbsp;) bằng khoảng trắng
        noHtml = noHtml.replaceAll("&nbsp;", " ");

        // 3. Loại bỏ khoảng trắng dư thừa đầu và cuối
        return noHtml.trim();
    }


    /* =======================================================================
     *  Hàm parseEmailContent mới - linh hoạt & dễ bảo trì hơn
     * ===================================================================== */
    private TransportUnitEmailRequest parseEmailContent(String rawContent, String defaultSenderEmail) {
        String emailContent = rawContent
                .replaceAll("[\\u00A0\\u200B]", " ")
                .replace("\r", "")
                .replaceAll("[ \\t]{2,}", " ")
                .trim();

        final int FLAGS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.DOTALL;

        Pattern companyP     = Pattern.compile("Tên\\s*Công\\s*ty\\s*Vận\\s*(?:Tải|chuyển).*?\\[\\s*([^]]+?)\\s*]", FLAGS);
        Pattern contactP     = Pattern.compile("Tên\\s*Người\\s*Đại\\s*diện\\s*Liên\\s*hệ.*?\\[\\s*([^]]+?)\\s*]", FLAGS);
        Pattern phoneP       = Pattern.compile("Số\\s*Điện\\s*Thoại\\s*Liên\\s*hệ.*?\\[\\s*([^]]+?)\\s*]", FLAGS);
        Pattern licenseP     = Pattern.compile("(?:Biển|Bằng).*?Vận\\s*(?:Tải|Chuyển).*?\\[\\s*([^]]+?)\\s*]", FLAGS);
        Pattern noteP        = Pattern.compile("Ghi\\s*Chú\\s*Thêm.*?\\[\\s*([^]]+?)\\s*]", FLAGS);
        Pattern emailP       = Pattern.compile("[\\[(]([a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,6})[\\])]", FLAGS);
        Pattern quantityP    = Pattern.compile("Số\\s*lượng\\s*xe.*?\\[\\s*(\\d{1,4})\\s*]", FLAGS);
        Pattern capacityP    = Pattern.compile("(?:Thể\\s*tích|Sức\\s*chứa).*?\\[\\s*([\\d.]+)\\s*m3?\\s*]", FLAGS);
        Pattern statusP      = Pattern.compile("Tình\\s*trạng\\s*xe.*?\\[\\s*([^]]+?)\\s*]", FLAGS);

        TransportUnitEmailRequest req = new TransportUnitEmailRequest();
        req.setNameCompany(find(emailContent, companyP));
        req.setNamePersonContact(find(emailContent, contactP));
        req.setPhone(find(emailContent, phoneP));
        req.setLicensePlate(find(emailContent, licenseP));
        req.setNote(Optional.ofNullable(find(emailContent, noteP)).orElse(""));

        String quantityStr = find(emailContent, quantityP);
        String capacityStr = find(emailContent, capacityP);
        String availabilityStr = find(emailContent, statusP);
        String sender = find(emailContent, emailP);

        if (sender == null || sender.isBlank()) sender = defaultSenderEmail;
        if (sender == null || sender.isBlank()) sender = mailUsername;
        req.setSenderEmail(sender);

        try {
            if (quantityStr != null) {
                req.setNumberOfVehicles(Integer.parseInt(quantityStr));
            }
            if (capacityStr != null) {
                req.setCapacityPerVehicle(Double.parseDouble(capacityStr));
            }
            if (availabilityStr != null) {
                req.setAvailabilityStatus(TransportAvailabilityStatus.valueOf(
                        availabilityStr.trim().toUpperCase().replace(" ", "_")
                ));
            } else {
                req.setAvailabilityStatus(TransportAvailabilityStatus.AVAILABLE);
            }
        } catch (Exception e) {
            System.err.println("❌ Parse số/thể tích/trạng thái thất bại: " + e.getMessage());
        }

        return req;
    }



    /* Helper: trả về group(1) nếu match, null nếu không */
    private static String find(String src, Pattern p) {
        Matcher m = p.matcher(src);
        return m.find() ? m.group(1).trim() : null;
    }
    public void checkEmailsAndOnboard() {
        Properties properties = new Properties();
        properties.put("mail.imap.host", imapHost);
        properties.put("mail.imap.port", imapPort);
        properties.put("mail.imap.ssl.enable", "true");
        properties.put("mail.imap.auth", "true");
        properties.put("mail.mime.charset", "UTF-8");
        properties.put("mail.imaps.partialfetch", "false");

        Session emailSession = Session.getDefaultInstance(properties);
        Store store = null;
        Folder inbox = null;

        try {
            store = emailSession.getStore("imap");
            store.connect(imapHost, mailUsername, mailPassword);
            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            LocalDateTime since = LocalDateTime.now().minusHours(24);
            Date date = Date.from(since.atZone(ZoneId.systemDefault()).toInstant());
            SearchTerm searchTerm = new AndTerm(
                    new ReceivedDateTerm(ComparisonTerm.GT, date),
                    new FlagTerm(new Flags(Flags.Flag.SEEN), false)
            );

            Message[] messages = inbox.search(searchTerm);
            System.out.println("Found " + messages.length + " unread emails in last 24h");
            int maxEmailsToProcess = 20;
            int total = Math.min(messages.length, maxEmailsToProcess);
            for (int i = 0; i < total; i++) {
                Message message = messages[i];
                String subject = message.getSubject();
                String sender = extractSender(message);
                if (sender.equalsIgnoreCase(mailUsername)) {
                    System.out.println("⚠️ Bỏ qua email gửi từ hệ thống: " + sender);
                    message.setFlag(Flags.Flag.SEEN, true);
                    continue;
                }

                if (subject == null || !subject.contains("[ĐĂNG KÝ ĐƠN VỊ VẬN CHUYỂN MỚI]")) {
                    sendSuggestionEmailToSender(sender);
                    message.setFlag(Flags.Flag.SEEN, true);
                    continue;
                }

                System.out.println("\n--- Email " + (i + 1) + "/" + messages.length + " ---");
                String content = getTextFromMessage(message);
                TransportUnitEmailRequest request = parseEmailContent(content, sender);

// Nếu request null hoặc không hợp lệ → đánh dấu SEEN và bỏ qua, không gửi lại email phản hồi nữa
                if (request == null || !isValidRequest(request)) {
                    System.err.println("❌ Email không hợp lệ, không gửi phản hồi lặp lại.");
                    message.setFlag(Flags.Flag.SEEN, true);
                    continue;
                }

                // Step 1: Ưu tiên file đính kèm
                byte[] frontBytes = null;
                byte[] backBytes = null;

                if (message.isMimeType("multipart/*")) {
                    Multipart mp = (Multipart) message.getContent();
                    int index = 0;
                    for (int j = 0; j < mp.getCount(); j++) {
                        BodyPart part = mp.getBodyPart(j);
                        if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                            byte[] data = part.getInputStream().readAllBytes();
                            if (index == 0) frontBytes = data;
                            else if (index == 1) backBytes = data;
                            index++;
                        }
                    }
                }

                // Step 2: Nếu không có file thì thử tải từ link (nếu có)
                if (frontBytes == null && request.getCertificateFrontUrl() != null) {
                    frontBytes = downloadImageFromUrl(request.getCertificateFrontUrl());
                }
                if (backBytes == null && request.getCertificateBackUrl() != null) {
                    backBytes = downloadImageFromUrl(request.getCertificateBackUrl());
                }

                // Step 3: Upload lên Cloudinary
                try {
                    if (frontBytes != null) {
                        String url = cloudinaryService.uploadImage(frontBytes, "certificate_front_" + System.currentTimeMillis());
                        request.setCertificateFrontUrl(url);
                    }
                    if (backBytes != null) {
                        String url = cloudinaryService.uploadImage(backBytes, "certificate_back_" + System.currentTimeMillis());
                        request.setCertificateBackUrl(url);
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Upload ảnh thất bại: " + e.getMessage());
                }

                // Step 4: Gửi về API
                if (request != null && isValidRequest(request)) {
                    sendToOnboardingApi(request);
                } else {
                    sendSuggestionEmailToSender(sender);
                }


                message.setFlag(Flags.Flag.SEEN, true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inbox != null && inbox.isOpen()) inbox.close(true);
                if (store != null && store.isConnected()) store.close();
            } catch (MessagingException e) {
                System.err.println("❌ Lỗi khi đóng email: " + e.getMessage());
            }
        }
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
            message.setSubject("Mẫu đăng ký đơn vị vận chuyển");

            String body = """
                Vui lòng điền đúng mẫu sau đây và gửi lại:
                --------------------------------------------
                [ĐĂNG KÝ ĐƠN VỊ VẬN CHUYỂN MỚI]
                --------------------------------------------
                Tên Công ty Vận chuyển: [Tên công ty]
                Tên Người Đại diện Liên hệ: [Họ và tên]
                Số Điện Thoại Liên hệ: [SĐT]
                Email Liên hệ: [email liên hệ]
                Giấy phép Vận chuyển: [ABC]
                Số lượng xe: [15]
                Thể tích xe: [18.5 m3]
                Ghi Chú Thêm: [Tùy chọn]
                --------------------------------------------
                LƯU Ý: Hãy điền thông tin trong dấu ngoặc vuông nhé:>>
                       Gửi bằng cấp xin hãy gửi link ảnh bên dưới.
            """;

            message.setText(body);
            Transport.send(message);
            System.out.println("✅ Đã gửi mẫu đăng ký lại cho người gửi: " + to);

        } catch (Exception e) {
            System.err.println("❌ Gửi email mẫu thất bại: " + e.getMessage());
        }
    }
    private byte[] downloadImageFromUrl(String imageUrl) {
        try (java.io.InputStream in = new java.net.URL(imageUrl).openStream()) {
            return in.readAllBytes();
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi tải ảnh từ URL: " + imageUrl + " - " + e.getMessage());
            return null;
        }
    }
    private boolean isValidRequest(TransportUnitEmailRequest req) {


        return req.getNameCompany() != null && !req.getNameCompany().isBlank()
                && req.getNamePersonContact() != null && !req.getNamePersonContact().isBlank()
                && req.getPhone() != null && !req.getPhone().isBlank()
                && req.getSenderEmail() != null && !req.getSenderEmail().isBlank()
                && req.getLicensePlate() != null && !req.getLicensePlate().isBlank()
                && req.getNumberOfVehicles() != null
                && req.getCapacityPerVehicle() != null;
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