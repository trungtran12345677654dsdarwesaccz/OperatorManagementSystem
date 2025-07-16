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
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
    public String confirmPayment() {
        List<String> messages = oauthGmail.listLatestEmails(1);
        for (String msg : messages) {
            System.out.println("Email content: " + msg);
        }
        List<Transaction> transactions = parseTransactions(messages);
        if (transactions == null || transactions.isEmpty()) {
            throw new RuntimeException("No transactions found");
        }

        Users currentUser = getCurrentUser();

        for (Transaction tx : transactions) {
            Integer bookingId;
            try {
                bookingId = Integer.valueOf(tx.getId());
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid booking id format: " + tx.getId());
            }

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found for id: " + bookingId));

            if (booking.getTotal() == null || booking.getTotal().compareTo(BigDecimal.valueOf(tx.getAmount())) != 0) {
                throw new RuntimeException("Amount mismatch for transaction ID: " + tx.getId());
            }

            if (!payment.getPayer().getId().equals(currentUser.getId())) {
                throw new RuntimeException("User mismatch for transaction ID: " + tx.getId());
            }

            // Tạo mới đối tượng Payment
            Payment payment = Payment.builder()
                    .booking(booking)
                    .payer(currentUser)
                    .amount(BigDecimal.valueOf(tx.getAmount()))
                    .paidDate(LocalDate.now())
                    .transactionNo(tx.getDescription())
                    .build();

            paymentRepository.save(payment);



            booking.setPaymentStatus(PaymentStatus.COMPLETED); // hoặc COMPLETED tùy enum của bạn
            bookingRepository.save(booking);



        }

        return "Confirmed Successfully";
    }


    private List<Transaction> parseTransactions(List<String> messages) {
        List<Transaction> transactions = new ArrayList<>();

        for (String message : messages) {
            // 1. Lấy số tiền sau "GD: "
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

            // 2. Lấy số sau "BOOKING"
            Pattern bookingIdPattern = Pattern.compile("BOOKING(\\d+)");
            Matcher bookingIdMatcher = bookingIdPattern.matcher(message);
            if (!bookingIdMatcher.find()) continue;

            String rawIdStr = bookingIdMatcher.group(1);

            Integer rawId;
            try {
                rawId = Integer.valueOf(rawIdStr);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid id format: " + rawIdStr);
            }

            transactions.add(new Transaction(rawId, amount, "SMS"));
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
