package org.example.operatormanagementsystem.controller;

import org.example.operatormanagementsystem.entity.Payment;
import org.example.operatormanagementsystem.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Integer id) {
        Optional<Payment> payment = paymentService.getPaymentById(id);
        return payment.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Payment createPayment(@RequestBody Payment payment) {
        return paymentService.savePayment(payment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Payment> updatePayment(@PathVariable Integer id, @RequestBody Payment updatedPayment) {
        return paymentService.getPaymentById(id)
                .map(payment -> {
                    // Update các field cho đúng (hoặc dùng modelMapper)
                    payment.setBooking(updatedPayment.getBooking());
                    payment.setPayerType(updatedPayment.getPayerType());
                    payment.setPayerId(updatedPayment.getPayerId());
                    payment.setAmount(updatedPayment.getAmount());
                    payment.setPaidDate(updatedPayment.getPaidDate());
                    payment.setStatus(updatedPayment.getStatus());
                    payment.setNote(updatedPayment.getNote());
                    Payment saved = paymentService.savePayment(payment);
                    return ResponseEntity.ok(saved);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Integer id) {
        if (paymentService.getPaymentById(id).isPresent()) {
            paymentService.deletePayment(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public List<Payment> searchPayments(@RequestParam("q") String keyword) {
        return paymentService.searchPayments(keyword);
    }
}
