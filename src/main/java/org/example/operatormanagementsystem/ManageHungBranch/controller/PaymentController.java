package org.example.operatormanagementsystem.ManageHungBranch.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.operatormanagementsystem.ManageHungBranch.dto.PaymentDTO;
import org.example.operatormanagementsystem.ManageHungBranch.service.PaymentService;
import org.example.operatormanagementsystem.entity.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/payments")
@Tag(name = "Receipt Management", description = "API tiếp nhận hóa đơn khách hàng - Manage Customer Receipts Received by Staff")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    // GET: Lấy danh sách tất cả payment receipts
    @GetMapping
    public ResponseEntity<List<PaymentDTO>> getAllPayments() {
        List<PaymentDTO> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }

    // GET: Lấy chi tiết payment receipt theo ID
    @GetMapping("/{id}")
    public ResponseEntity<PaymentDTO> getPaymentById(@PathVariable Integer id) {
        PaymentDTO payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(payment);
    }

    // GET: Tìm kiếm payment receipts theo status
    @GetMapping("/search")
    public ResponseEntity<List<PaymentDTO>> searchPaymentsByStatus(@RequestParam String status) {
        List<PaymentDTO> payments = paymentService.searchPaymentsByStatus(status);
        return ResponseEntity.ok(payments);
    }

    // GET: Tìm kiếm payment receipts theo payer type
    @GetMapping("/search/payer-type")
    public ResponseEntity<List<PaymentDTO>> searchPaymentsByPayerType(@RequestParam String payerType) {
        List<PaymentDTO> payments = paymentService.searchPaymentsByPayerType(payerType);
        return ResponseEntity.ok(payments);
    }

    // POST: Tạo payment receipt mới (Staff nhận payment từ customer)
    @PostMapping
    public ResponseEntity<PaymentDTO> createPayment(@RequestBody PaymentDTO paymentDTO) {
        PaymentDTO createdPayment = paymentService.createPayment(paymentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPayment);
    }

    // PUT: Cập nhật payment receipt
    @PutMapping("/{id}")
    public ResponseEntity<PaymentDTO> updatePayment(@PathVariable Integer id, @RequestBody PaymentDTO paymentDTO) {
        PaymentDTO updatedPayment = paymentService.updatePayment(id, paymentDTO);
        return ResponseEntity.ok(updatedPayment);
    }

    // DELETE: Xóa payment receipt
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Integer id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }
}
//POST: Tạo payment mới với JSON như ví dụ trên
//GET: Xem danh sách payments
//GET /{id}: Xem chi tiết payment
//PUT /{id}: Cập nhật payment
//DELETE /{id}: Xóa payment
//GET /search: Test tìm kiếm theo status
//GET /search/payer-type: Test tìm kiếm theo payer type