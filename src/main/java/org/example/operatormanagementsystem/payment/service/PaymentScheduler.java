package org.example.operatormanagementsystem.payment.service;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.payment.service.PaymentService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentScheduler {

    private final PaymentService paymentService;

    @Scheduled(fixedRate = 3000)
    public void autoConfirmPayment() {
        try {
            String result = paymentService.confirmPayment();
        } catch (Exception e) {
//            System.err.println("[SCHEDULED] Error: " + e.getMessage());
        }
    }
}