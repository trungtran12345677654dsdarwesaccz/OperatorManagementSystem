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
import java.util.ArrayList;
import java.util.List;
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
    private final OauthGmail oauthGmail;
    private final BookingRepository bookingRepository;


    @Override
    public PaymentReturnUrl createQr(CreatePaymentRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
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
        List<Transaction> transactions = parseTransactions(messages);

        if (transactions == null || transactions.isEmpty()) {
            throw new RuntimeException("No transactions found");
        }

        Users user = getCurrentUser();

        for (Transaction transaction : transactions) {
            Payment payment = paymentRepository.findById(Integer.valueOf(transaction.getId()))
                    .orElseThrow(() -> new RuntimeException("Payment not found for ID: " + transaction.getId()));

            if (payment.getAmount().compareTo(BigDecimal.valueOf(transaction.getAmount())) != 0) {
                throw new RuntimeException("Amount mismatch");
            }

            if (!payment.getPayer().getId().equals(user.getId())) {
                throw new RuntimeException("User mismatch");
            }

            // Cập nhật trạng thái thanh toán của booking thành COMPLETED
            Booking booking = payment.getBooking();
            booking.setPaymentStatus(PaymentStatus.COMPLETED);
            bookingRepository.save(booking);

            paymentRepository.save(payment);
        }

        return "Confirmed Successfully";
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

    private List<Transaction> parseTransactions(List<String> messages) {
        List<Transaction> transactions = new ArrayList<>();
        for (String message : messages) {
            if (!message.contains("KL")) continue;

            Pattern amountPattern = Pattern.compile("PS:\\s*\\+([\\d,]+)\\s*VND");
            Matcher amountMatcher = amountPattern.matcher(message);
            if (!amountMatcher.find()) continue;
            String rawAmount = amountMatcher.group(1).replace(",", "");
            double amount = Double.parseDouble(rawAmount);

            Pattern idPattern = Pattern.compile("KL_(\\d+)");
            Matcher idMatcher = idPattern.matcher(message);
            if (!idMatcher.find()) continue;
            String id = idMatcher.group(1);

            Transaction tx = new Transaction();
            tx.setAmount(amount);
            tx.setId(id);
            tx.setDescription("KL");
            transactions.add(tx);
        }
        return transactions;
    }
}
