package org.example.operatormanagementsystem.payment.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.ManageHungBranch.repository.PaymentRepository;
import org.example.operatormanagementsystem.config.JwtUtil;
import org.example.operatormanagementsystem.config.OauthGmail;
import org.example.operatormanagementsystem.entity.Booking;
import org.example.operatormanagementsystem.entity.Payment;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.enumeration.PaymentStatus;
import org.example.operatormanagementsystem.managecustomerorderbystaff.repository.BookingRepository;
import org.example.operatormanagementsystem.payment.dto.PaymentReturnUrl;
import org.example.operatormanagementsystem.payment.dto.request.CreatePaymentRequest;
import org.example.operatormanagementsystem.payment.dto.response.Transaction;
import org.example.operatormanagementsystem.payment.service.PaymentService;
import org.example.operatormanagementsystem.payment.utils.VietQrProperties;
import org.example.operatormanagementsystem.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final HttpServletRequest request;
    private final VietQrProperties vietQrProperties;
    private final BookingRepository bookingRepository;
    private final  OauthGmail oauthGmail;

    @Override
    public PaymentReturnUrl createQr(CreatePaymentRequest req) {
        Booking booking = bookingRepository.findById(req.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy booking"));

        Long totalValue = booking.getTotal();
        if (totalValue == null || totalValue <= 0) {
            throw new IllegalArgumentException("Giá trị tổng tiền trong booking không hợp lệ");
        }

        BigDecimal amount = BigDecimal.valueOf(totalValue);

        String note = booking.getNote();
        if (note == null || note.isBlank()) {
            note = "BOOKING";
        }

        String encodedAccountName = URLEncoder.encode(vietQrProperties.getAccountName(), StandardCharsets.UTF_8);
        String encodedNote = URLEncoder.encode(note, StandardCharsets.UTF_8);

        String qrUrl = String.format(
                "https://img.vietqr.io/image/%s-%s-qr_only.png?amount=%d&addInfo=%s&accountName=%s",
                vietQrProperties.getBankId(),
                vietQrProperties.getAccountNumber(),
                amount.longValue(),
                encodedNote,
                encodedAccountName
        );

        return new PaymentReturnUrl(qrUrl, note, amount);
    }


    @Override
    @Transactional
    public String confirmPayment() {
        // Lấy danh sách email mới nhất
        List<String> messages = oauthGmail.listLatestEmails(1);
        for (String msg : messages) {
            System.out.println("Email content: " + msg);
        }

        // Phân tích các giao dịch từ nội dung email
        List<Transaction> transactions = parseTransactions(messages);
        if (transactions == null || transactions.isEmpty()) {
            throw new RuntimeException("No transactions found");
        }

        // Lấy người dùng hiện tại
        Users currentUser = getCurrentUser();

        for (Transaction tx : transactions) {
            Integer bookingId = tx.getId();

            // Tìm kiếm booking tương ứng
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found for id: " + bookingId));

            BigDecimal total = BigDecimal.valueOf(booking.getTotal());
            BigDecimal amount = BigDecimal.valueOf(tx.getAmount());

            if (total.compareTo(amount) != 0) {
                throw new RuntimeException("Amount mismatch for booking ID: " + bookingId);
            }


            // Tạo đối tượng Payment mới, transactionNo lấy từ nội dung note (description)
            Payment payment = Payment.builder()
                    .booking(booking)
                    .payer(currentUser)
                    .amount(BigDecimal.valueOf(tx.getAmount()))
                    .paidDate(LocalDate.now())
                    .transactionNo(generateRandom6Digits())
                    .build();

            paymentRepository.save(payment);

            // Cập nhật trạng thái thanh toán booking
            booking.setPaymentStatus(PaymentStatus.COMPLETED);
            bookingRepository.save(booking);
        }

        return "Confirmed Successfully";
    }
    public String generateRandom6Digits() {
        Random random = new Random();
        int number = 100000 + random.nextInt(900000); // sinh số từ 100000 đến 999999
        return String.valueOf(number);
    }

    private List<Transaction> parseTransactions(List<String> messages) {
        List<Transaction> transactions = new ArrayList<>();

        for (String message : messages) {
            // Lấy số tiền sau "GD: "
            Pattern amountPattern = Pattern.compile("GD:\\s*([+-][\\d,]+)VND");
            Matcher amountMatcher = amountPattern.matcher(message);
            if (!amountMatcher.find()) continue;

            String rawAmount = amountMatcher.group(1).replace(",", "");
            Double amount;
            try {
                amount = Double.parseDouble(rawAmount);
            } catch (NumberFormatException e) {
                continue;
            }

            // Lấy số bookingId sau "BOOKING"
            Pattern bookingIdPattern = Pattern.compile("BOOKING(\\d+)");
            Matcher bookingIdMatcher = bookingIdPattern.matcher(message);
            if (!bookingIdMatcher.find()) continue;

            String rawIdStr = bookingIdMatcher.group(1);

            Integer rawId;
            try {
                rawId = Integer.valueOf(rawIdStr);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid booking id format: " + rawIdStr);
            }

            // Lấy nguyên nội dung message làm note (bạn có thể chỉnh sửa nếu cần lấy đoạn cụ thể hơn)
            String note = message;

            // Tạo Transaction với bookingId, amount, và note
            transactions.add(new Transaction(rawId, amount, note));
        }

        return transactions;
    }



    private Users getCurrentUser() {
        String token = jwtUtil.resolveToken(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            throw new RuntimeException("Token không hợp lệ hoặc đã hết hạn");
        }
        String email = jwtUtil.extractUsername(token);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email: " + email));
    }
}
