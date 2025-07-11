package org.example.operatormanagementsystem.payment.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.payment.dto.BookingQRResponse;
import org.example.operatormanagementsystem.payment.dto.SmsMessageDto;
import org.example.operatormanagementsystem.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@PreAuthorize("hasAnyRole('CUSTOMER')")
@RequiredArgsConstructor
public class PaymentQRController {

    private final PaymentService paymentService;

    @PostMapping("/sms-callback")
    public ResponseEntity<String> handleSmsCallback(@RequestBody SmsMessageDto sms,  HttpServletRequest request) {
        System.out.println("📩 [SMS Callback] Received: " + sms.getMessage());
        String result = paymentService.confirmPaymentFromSms(sms, request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/generate-vietqr/{bookingId}")
    public ResponseEntity<BookingQRResponse> generateVietQr(@PathVariable Integer bookingId) {
        return ResponseEntity.ok(paymentService.generateVietQrForBooking(bookingId));
    }
}
