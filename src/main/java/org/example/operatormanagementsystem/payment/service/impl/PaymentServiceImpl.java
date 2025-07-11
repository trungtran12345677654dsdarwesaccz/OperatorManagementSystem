package org.example.operatormanagementsystem.payment.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.ManageHungBranch.repository.PaymentRepository;
import org.example.operatormanagementsystem.config.JwtUtil;
import org.example.operatormanagementsystem.entity.*;
import org.example.operatormanagementsystem.enumeration.PaymentStatus;
import org.example.operatormanagementsystem.managecustomerorderbystaff.repository.BookingRepository;
import org.example.operatormanagementsystem.payment.dto.BookingQRResponse;
import org.example.operatormanagementsystem.payment.dto.SmsMessageDto;
import org.example.operatormanagementsystem.payment.service.PaymentService;
import org.example.operatormanagementsystem.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public String confirmPaymentFromSms(SmsMessageDto sms, HttpServletRequest request) {
        String content = sms.getMessage();
        String timestamp = sms.getTimestamp();

        System.out.println("📩 Nội dung SMS: " + content);
        System.out.println("🕒 Thời gian: " + (timestamp != null ? timestamp : "Không có"));

        BigDecimal amount = extractAmount(content);
        String note = extractNote(content);

        System.out.println("💰 Amount = " + amount);
        System.out.println("📝 Note = '" + note + "'");

        if (amount.compareTo(BigDecimal.ZERO) <= 0 || note.isEmpty()) {
            return "⚠️ Không thể xác định số tiền hoặc mã booking từ SMS.";
        }

        Optional<Booking> optionalBooking = bookingRepository
                .findByPaymentStatusAndTotalAndNote(PaymentStatus.INCOMPLETED, amount.longValue(), note);

        if (optionalBooking.isEmpty()) {
            System.out.println("❌ Không tìm thấy booking phù hợp.");
            return "❌ Không tìm thấy booking phù hợp để xác nhận thanh toán.";
        }

        Booking booking = optionalBooking.get();
        booking.setPaymentStatus(PaymentStatus.COMPLETED);
        bookingRepository.save(booking);

        Users currentUser = resolveUser(request);

        Payment payment = Payment.builder()
                .booking(booking)
                .amount(BigDecimal.valueOf(booking.getTotal()))
                .paidDate(LocalDate.now())
                .status(PaymentStatus.COMPLETED)
                .note(note)
                .payer(currentUser)
                .transactionNo("SMS_" + System.currentTimeMillis())
                .build();

        paymentRepository.save(payment);

        return "✅ Đã xác nhận thanh toán cho booking #" + booking.getBookingId();
    }

    private Users resolveUser(HttpServletRequest request) {
        if (request == null) {
            return userRepository.findByEmail("system@backend.local")
                    .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng hệ thống"));
        }
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String email = jwtUtil.extractUsername(token);
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng từ token"));
        } catch (Exception e) {
            throw new RuntimeException("Không xác thực được người dùng từ JWT", e);
        }
    }

    private BigDecimal extractAmount(String msg) {
        Matcher m = Pattern.compile("\\+(\\d+(?:,\\d{3})*)").matcher(msg);
        return m.find() ? new BigDecimal(m.group(1).replace(",", "")) : BigDecimal.ZERO;
    }

    private String extractNote(String msg) {
        Matcher m = Pattern.compile("(BOOKING\\s?\\d+)").matcher(msg.toUpperCase());
        return m.find() ? m.group(1).replace(" ", "").trim() : "";
    }

    @Override
    public BookingQRResponse generateVietQrForBooking(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy booking"));

        String bankCode = "MB"; // MB = MBBank
        String accountNumber = "0123317466666";
        String accountName = "NGUYEN VAN PHONG";

        String note = booking.getNote();
        BigDecimal amount = BigDecimal.valueOf(booking.getTotal());

        String qrUrl = String.format(
                "https://img.vietqr.io/image/%s-%s-qr_only.png?amount=%d&addInfo=%s&accountName=%s",
                bankCode,
                accountNumber,
                amount.longValue(),
                note,
                URLEncoder.encode(accountName, StandardCharsets.UTF_8)
        );

        return new BookingQRResponse(qrUrl, note, amount);
    }
}
