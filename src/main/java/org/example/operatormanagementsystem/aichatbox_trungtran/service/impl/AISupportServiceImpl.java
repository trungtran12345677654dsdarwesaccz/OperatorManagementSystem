    package org.example.operatormanagementsystem.aichatbox_trungtran.service.impl;

    import lombok.RequiredArgsConstructor;
    import org.example.operatormanagementsystem.ManageHungBranch.repository.PaymentRepository;
    import org.example.operatormanagementsystem.aichatbox_trungtran.dto.request.AnswerRequest;
    import org.example.operatormanagementsystem.aichatbox_trungtran.dto.request.GeminiRequest;
    import org.example.operatormanagementsystem.aichatbox_trungtran.dto.response.GeminiResponse;
    import org.example.operatormanagementsystem.aichatbox_trungtran.service.AISupportService;
    import org.example.operatormanagementsystem.config.JwtUtil;
    import org.example.operatormanagementsystem.customer_thai.repository.BookingCustomerRepository;
    import org.example.operatormanagementsystem.entity.Booking;
    import org.example.operatormanagementsystem.entity.Payment;
    import org.example.operatormanagementsystem.listProfileTrungTran.dto.response.ViewProfileResponse;
    import org.example.operatormanagementsystem.listProfileTrungTran.service.ViewProfileService;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.http.ResponseEntity;
    import org.springframework.stereotype.Service;
    import org.springframework.web.client.RestTemplate;
    import jakarta.servlet.http.HttpServletRequest;

    import java.util.Arrays;
    import java.util.Collections;
    import java.util.List;
    import java.util.Optional;

    @Service
    @RequiredArgsConstructor
    public class AISupportServiceImpl implements AISupportService {

        private final PaymentRepository paymentRepository;
        private final BookingCustomerRepository bookingRepository;
        private final ViewProfileService viewProfileService;
        private final RestTemplate restTemplate;
        private final JwtUtil jwtUtil;
        private final HttpServletRequest request;

        private final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
        private static final String DEFAULT_GEMINI_MODEL = "gemini-2.0-flash";

        @Value("${gemini.api.key}")
        private String apiKey;

        @Override
        public String answerFromGemini(AnswerRequest answerRequest) {
            String question = answerRequest.getQuestion();

            if (isInappropriateQuestion(question)) {
                return "❗ Tôi không thể hỗ trợ yêu cầu này. Vui lòng đặt câu hỏi liên quan đến dịch vụ chuyển nhà hoặc thông tin đơn hàng.";
            }

            if (isBookingStatusQuestion(question)) {
                return generateBookingStatusReply();
            }

            if (isPaymentHistoryQuestion(question)) {
                return generatePaymentHistoryReply();
            }


            String fallbackPrompt = String.format("""
                    Bạn là một nhân viên tư vấn AI đang làm việc trong hệ thống đặt xe chuyển nhà tại Việt Nam. 
                     Khách hàng vừa hỏi: "%s"
                     Hãy trả lời ngắn gọn, rõ ràng, chuyên nghiệp và đúng trọng tâm. 
                     Gợi ý khách sử dụng dịch vụ một cách tự nhiên và thân thiện. 
                     Nếu có thể, hãy giới thiệu các điểm nổi bật như:
                     - Hỗ trợ chuyển điều hòa, tủ lạnh, máy giặt, đồ dễ vỡ...
                     - Có nhân viên bọc lót, tháo lắp và sắp xếp đồ tại chỗ.
                     - Theo dõi đơn hàng, tài xế, chi phí rõ ràng qua app.
                     - Đội ngũ tài xế chuyên nghiệp, phục vụ tận tình, đúng giờ.
                     - Dịch vụ có mặt tại nhiều tỉnh thành, hỗ trợ nhanh chóng.
                    
                     Mục tiêu là làm khách hàng cảm thấy yên tâm và hài lòng khi sử dụng dịch vụ chuyển nhà của hệ thống.
            """, question);

            String result = queryGemini(fallbackPrompt);

            if (result == null || result.trim().isEmpty()
                    || result.toLowerCase().contains("i'm not sure")
                    || result.toLowerCase().contains("i cannot")
                    || result.toLowerCase().contains("i don’t know")
                    || result.toLowerCase().contains("i do not know")) {
                return "❗ Câu hỏi của bạn hiện không nằm trong phạm vi công việc của tôi. Cảm ơn bạn đã đặt câu hỏi, chúng tôi sẽ cải thiện hệ thống để phục vụ bạn tốt hơn.";
            }

            return result;
        }

        private String generatePaymentHistoryReply() {
            String email = getCurrentCustomerEmailFromToken();
            List<Payment> payments = paymentRepository.findTop3ByBooking_Customer_Users_EmailOrderByPaidDateDesc(email);
            if (payments.isEmpty()) {
                return "Bạn chưa có thanh toán nào được ghi nhận.";
            }
            StringBuilder sb = new StringBuilder("💳 Lịch sử thanh toán gần nhất:\n");
            for (Payment p : payments) {
                sb.append("- Ngày: ").append(p.getPaidDate())
                        .append(" | Số tiền: ").append(p.getAmount()).append(" VNĐ")
                        .append(" | Trạng thái: ").append(p.getBooking().getPaymentStatus()
                        )
                        .append("\n");
            }
            return sb.toString();
        }

        private String generateBookingStatusReply() {
            String email = getCurrentCustomerEmailFromToken();
            Optional<Booking> latestBookingOpt = bookingRepository.findTopByCustomer_Users_EmailOrderByCreatedAtDesc(email).stream().findFirst();
            if (latestBookingOpt.isEmpty()) {
                return "Hiện tại bạn chưa có đơn hàng nào.";
            }
            Booking booking = latestBookingOpt.get();
            StringBuilder sb = new StringBuilder("📦 Thông tin đơn hàng gần nhất của bạn:\n");
            sb.append("- Mã đơn hàng: ").append(booking.getBookingId()).append("\n")
                    .append("- Trạng thái: ").append(booking.getStatus()).append("\n")
                    .append("- Ngày tạo: ").append(booking.getCreatedAt()).append("\n")
                    .append("- Nơi lấy hàng: ").append(booking.getPickupLocation()).append("\n")
                    .append("- Nơi giao hàng: ").append(booking.getDeliveryLocation()).append("\n")
                    .append("- Tổng chi phí: ").append(booking.getTotal()).append(" VNĐ");
            return sb.toString();
        }


        private String queryGemini(String prompt) {
            GeminiRequest.Part part = new GeminiRequest.Part(prompt);
            GeminiRequest.Content content = new GeminiRequest.Content(Collections.singletonList(part));
            GeminiRequest requestBody = new GeminiRequest(Collections.singletonList(content));
            String url = GEMINI_URL + DEFAULT_GEMINI_MODEL + ":generateContent?key=" + apiKey;

            int maxAttempts = 3;

            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                try {
                    GeminiResponse response = restTemplate.postForObject(url, requestBody, GeminiResponse.class);

                    if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
                        GeminiResponse.Content contentRes = response.getCandidates().get(0).getContent();
                        if (contentRes != null && contentRes.getParts() != null && !contentRes.getParts().isEmpty()) {
                            return contentRes.getParts().get(0).getText();
                        }
                    }

                    return "⚠️ Không thể tạo phản hồi từ hệ thống AI. Vui lòng thử lại sau.";
                } catch (Exception e) {
                    String errorMsg = e.getMessage() != null ? e.getMessage() : "";

                    // Nếu lỗi do quá tải (503), thử lại sau thời gian chờ
                    if (errorMsg.contains("503") && attempt < maxAttempts) {
                        try {
                            Thread.sleep(2000L * attempt); // 2s, 4s, 6s
                        } catch (InterruptedException ignored) {}
                    } else {
                        // Nếu không phải lỗi quá tải hoặc đã hết số lần thử
                        return "🚧 Hệ thống AI hiện đang quá tải hoặc gặp lỗi. Vui lòng thử lại sau ít phút.";
                    }
                }
            }

            return "🚧 Hệ thống AI hiện đang quá tải. Bạn có thể thử lại sau hoặc làm mới trang.";
        }


        private boolean isBookingStatusQuestion(String question) {
            String lower = question.toLowerCase();
            return lower.contains("đơn hàng") || lower.contains("trạng thái") ||
                    lower.contains("chuyển nhà") || lower.contains("đặt xe") ||
                    lower.contains("tài xế") || lower.contains("giao hàng");
        }

        private boolean isPaymentHistoryQuestion(String question) {
            String lower = question.toLowerCase();
            return lower.contains("lịch sử thanh toán")
                    || lower.contains("thanh toán gần nhất")
                    || lower.contains("đã trả tiền chưa")
                    || lower.contains("giao dịch")
                    || lower.contains("đã trả chưa");
        }

        private boolean isProfileInfoQuestion(String question) {
            String lower = question.toLowerCase();
            return lower.contains("thông tin cá nhân") || lower.contains("hồ sơ") || lower.contains("profile")
                    || lower.contains("email") || lower.contains("số điện thoại") || lower.contains("địa chỉ");
        }

        private boolean isChangePasswordRequestQuestion(String question) {
            String lower = question.toLowerCase();
            return lower.contains("đổi mật khẩu") || lower.contains("thay đổi mật khẩu") || lower.contains("reset password");
        }

        private String getCurrentCustomerEmailFromToken() {
            String token = jwtUtil.resolveToken(request);
            if (token != null && jwtUtil.validateToken(token)) {
                return jwtUtil.extractUsername(token);
            }
            return null;
        }
        private static final String[] BLACKLIST_KEYWORDS = {
                "tiền", "vay", "cho mượn", "chuyển khoản", "momo", "mbbank", "vietcombank",
                "yêu", "crush", "bạn trai", "tình cảm", "thất tình", "cô đơn",
                "phim", "ca sĩ", "idol", "nhạc", "tấu hài", "TikTok",
                "trời mưa", "nắng", "lạnh", "nhiệt độ", "thời tiết",
                "đmm", "vcl", "ngu", "chửi", "tục", "địt", "lol", "cút", "ăn", "uống", "thích", "ngủ"
        };

        private boolean isInappropriateQuestion(String question) {
            String lower = question.toLowerCase();
            return Arrays.stream(BLACKLIST_KEYWORDS).anyMatch(lower::contains);
        }



    }
