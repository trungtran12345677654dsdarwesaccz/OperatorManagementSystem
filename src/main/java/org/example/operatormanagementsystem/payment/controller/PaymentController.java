    package org.example.operatormanagementsystem.payment.controller;
    
    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor;
    import org.example.operatormanagementsystem.payment.dto.PaymentReturnUrl;
    import org.example.operatormanagementsystem.payment.dto.request.CreatePaymentRequest;
    import org.example.operatormanagementsystem.payment.service.PaymentService;
    import org.springframework.http.ResponseEntity;
    import org.springframework.scheduling.annotation.Scheduled;
    import org.springframework.security.access.prepost.PreAuthorize;
    import org.springframework.web.bind.annotation.*;
    
    @RestController("paymentControllerMain")
    @RequestMapping("/api/payment")
    @RequiredArgsConstructor
    public class PaymentController {
    
        private final PaymentService paymentService;
        @PreAuthorize("hasAnyRole('CUSTOMER')")
        @PostMapping
        public ResponseEntity<PaymentReturnUrl> createQr( @RequestBody CreatePaymentRequest request) {
            return ResponseEntity.ok(paymentService.createQr(request));
        }
    
        @Scheduled(fixedRate = 3 * 1000)
        @PostMapping("/confirm-payment")
        public ResponseEntity<String> confirmPayment() {
            String result = paymentService.confirmPayment();
            return ResponseEntity.ok(result);
        }
    }
