package org.example.operatormanagementsystem.payment.service;

import org.example.operatormanagementsystem.payment.dto.PaymentReturnUrl;
import org.example.operatormanagementsystem.payment.dto.request.CreatePaymentRequest;

public interface PaymentService {
    PaymentReturnUrl createQr(CreatePaymentRequest request);
    String confirmPayment();
}
