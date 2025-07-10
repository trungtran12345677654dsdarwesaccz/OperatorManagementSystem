package org.example.operatormanagementsystem.payment.service;

import jakarta.servlet.http.HttpServletRequest;
import org.example.operatormanagementsystem.payment.dto.BookingQRResponse;
import org.example.operatormanagementsystem.payment.dto.SmsMessageDto;

public interface PaymentService {
    String confirmPaymentFromSms(SmsMessageDto sms, HttpServletRequest request);
    BookingQRResponse generateVietQrForBooking(Integer bookingId);
}
