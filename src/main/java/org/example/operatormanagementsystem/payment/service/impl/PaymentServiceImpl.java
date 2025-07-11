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
        BigDecimal amount = extractAmount(content);
        String note = extractNote(content);

        System.out.println("==> amount = " + amount);
        System.out.println("==> note = '" + note + "'");

        Optional<Booking> optionalBooking = bookingRepository
                .findByPaymentStatusAndTotalAndNote(PaymentStatus.INCOMPLETED, amount.longValue(), note);

        System.out.println("==> booking exists = " + optionalBooking.isPresent());

        if (optionalBooking.isEmpty()) {
            System.out.println("SMS content: " + sms.getMessage());
            System.out.println("Extracted amount: " + amount);
            System.out.println("Extracted note: " + note);
            return "Không tìm thấy booking phù hợp để xác nhận thanh toán.";
        }

        Booking booking = optionalBooking.get();
        booking.setPaymentStatus(PaymentStatus.COMPLETED);
        bookingRepository.save(booking);

        // Lấy thông tin người gửi
        Users currentUser;
        if (request != null) {
            try {
                String token = jwtUtil.extractTokenFromRequest(request);
                String email = jwtUtil.extractUsername(token);
                currentUser = userRepository.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng từ token"));
            } catch (Exception e) {
                throw new RuntimeException("Không xác thực được người dùng từ JWT", e);
            }
        } else {
            // fallback người dùng hệ thống
            currentUser = userRepository.findByEmail("system@backend.local")
                    .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng hệ thống"));
        }

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

        return "Đã xác nhận thanh toán cho booking #" + booking.getBookingId();
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

        String bankCode = "BIDV";
        String accountNumber = "4801011314";
        String accountName = "TRAN DUY TRUNG";

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
