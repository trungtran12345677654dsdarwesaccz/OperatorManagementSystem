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
        Booking booking = bookingRepository.findBookingByBookingId(req.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy booking"));
        System.out.println(">>> [createQr] bookingId = " + req.getBookingId());

        Long totalValue = booking.getTotal();
        System.out.println(">>> [createQr] totalAmount = " + booking.getTotal());
        if (totalValue == null || totalValue <= 0) {
            throw new IllegalArgumentException("Giá trị tổng tiền trong booking không hợp lệ: bookingId=" + req.getBookingId()
                    + ", amount=" + booking.getTotal());
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
        // 1. Lấy danh sách email mới nhất
        List<String> messages = oauthGmail.listLatestEmails(1);
        if( messages.size() < 1 || messages == null ) {
            throw new RuntimeException("Khong thay mail moi");

        }

        for (String msg : messages) {
            System.out.println("Email content: " + msg);
        }
        Users systemBot = userRepository.findByEmail("tranduytrung251105@gmail.com")
                .orElseThrow(() -> new RuntimeException("System bot user not found"));

        // 2. Phân tích giao dịch từ email
        List<Transaction> transactions = parseTransactions(messages);


        for (Transaction tx : transactions) {
            Integer bookingId = tx.getId();

            // 3. Tìm booking tương ứng
            Booking booking = bookingRepository.findBookingByBookingId(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found for id: " + bookingId));

            BigDecimal total = BigDecimal.valueOf(booking.getTotal());
            BigDecimal amount = BigDecimal.valueOf(tx.getAmount());

            if (total.compareTo(amount) != 0) {
                throw new RuntimeException("Amount mismatch for booking ID: " + bookingId);
            }

            if (paymentRepository.existsByBooking_BookingId(bookingId)) {
                System.out.println(" Booking #" + bookingId + " đã có payment, bỏ qua");
                continue;
            }

            // 4. Tạo đối tượng Payment
            Payment payment = Payment.builder()
                    .booking(booking)
                    .payer(systemBot)
                    .amount(amount)
                    .status(PaymentStatus.COMPLETED)
                    .paidDate(LocalDate.now())
                    .transactionNo(generateRandom6Digits())
                    .build();

            paymentRepository.save(payment);


            // 5. Cập nhật trạng thái thanh toán của booking
            booking.setPaymentStatus(PaymentStatus.COMPLETED);
            booking.setStatus("SHIPPING");
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
            // 🧾 VD: GD: +510,000VND
            Pattern amountPattern = Pattern.compile("GD:\\s*([+-]?[\\d,.]+)VND");
            Matcher amountMatcher = amountPattern.matcher(message);
            if (!amountMatcher.find()) {
                System.out.println(" Không tìm thấy amount trong: " + message);
                continue;
            }

            String rawAmount = amountMatcher.group(1).replace(",", "");
            long amount;
            try {
                amount = Long.parseLong(rawAmount);
            } catch (NumberFormatException e) {
                System.out.println(" Lỗi khi parse amount: " + rawAmount);
                continue;
            }

            //  VD: BOOKING73
            Pattern bookingIdPattern = Pattern.compile("BOOKING(\\d+)");
            Matcher bookingIdMatcher = bookingIdPattern.matcher(message);
            if (!bookingIdMatcher.find()) {
                System.out.println(" Không tìm thấy Booking ID trong: " + message);
                continue;
            }

            int bookingId;
            try {
                bookingId = Integer.parseInt(bookingIdMatcher.group(1));
            } catch (NumberFormatException e) {
                System.out.println(" Booking ID không hợp lệ: " + bookingIdMatcher.group(1));
                continue;
            }

            String note = message; // có thể cắt lấy đoạn riêng nếu muốn

            transactions.add(new Transaction(bookingId, amount, note));
            System.out.println(" Parsed Transaction: bookingId=" + bookingId + ", amount=" + amount);
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
