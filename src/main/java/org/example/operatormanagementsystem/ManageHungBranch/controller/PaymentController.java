package org.example.operatormanagementsystem.ManageHungBranch.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.ManageHungBranch.dto.PaymentDTO;
import org.example.operatormanagementsystem.ManageHungBranch.dto.PaymentSearchDTO;
import org.example.operatormanagementsystem.ManageHungBranch.service.PaymentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "APIs for managing customer receipts and payments")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/search")
    @Operation(summary = "Search receipts", description = "Search payments by various criteria for staff to view")
    public ResponseEntity<Page<PaymentDTO>> searchReceipts(
            @Parameter(description = "Search criteria") PaymentSearchDTO searchDTO,
            Pageable pageable) {
        Page<PaymentDTO> payments = paymentService.searchPayments(searchDTO, pageable);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/list")
    @Operation(summary = "View receipts list", description = "Get paginated list of all payments")
    public ResponseEntity<Page<PaymentDTO>> getReceiptsList(
            @Parameter(description = "Page information") Pageable pageable) {
        Page<PaymentDTO> payments = paymentService.getAllPayments(pageable);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "View receipt detail", description = "Get detailed information of a specific payment")
    public ResponseEntity<PaymentDTO> getReceiptDetail(
            @Parameter(description = "Payment ID") @PathVariable Integer paymentId) {
        PaymentDTO payment = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(payment);
    }

    @PostMapping
    @Operation(summary = "Create new payment", description = "Create a new payment record")
    public ResponseEntity<PaymentDTO> createPayment(@RequestBody PaymentDTO paymentDTO) {
        PaymentDTO createdPayment = paymentService.createPayment(paymentDTO);
        return ResponseEntity.ok(createdPayment);
    }

    @PutMapping("/{paymentId}")
    @Operation(summary = "Update payment", description = "Update existing payment information")
    public ResponseEntity<PaymentDTO> updatePayment(
            @Parameter(description = "Payment ID") @PathVariable Integer paymentId,
            @RequestBody PaymentDTO paymentDTO) {
        PaymentDTO updatedPayment = paymentService.updatePayment(paymentId, paymentDTO);
        return ResponseEntity.ok(updatedPayment);
    }

    @DeleteMapping("/{paymentId}")
    @Operation(summary = "Delete payment", description = "Delete a payment record from database")
    public ResponseEntity<Void> deletePayment(
            @Parameter(description = "Payment ID") @PathVariable Integer paymentId) {
        paymentService.deletePayment(paymentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/overdue")
    @Operation(summary = "Get overdue payments", description = "Get list of overdue payments for staff follow-up")
    public ResponseEntity<List<PaymentDTO>> getOverduePayments() {
        List<PaymentDTO> overduePayments = paymentService.getOverduePayments();
        return ResponseEntity.ok(overduePayments);
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending payments", description = "Get list of pending payments")
    public ResponseEntity<List<PaymentDTO>> getPendingPayments() {
        List<PaymentDTO> pendingPayments = paymentService.getPendingPayments();
        return ResponseEntity.ok(pendingPayments);
    }
}
//POST: Tạo payment mới với JSON như ví dụ trên
//GET: Xem danh sách payments
//GET /{id}: Xem chi tiết payment
//PUT /{id}: Cập nhật payment
//DELETE /{id}: Xóa payment
//GET /search: Test tìm kiếm theo status
//GET /search/payer-type: Test tìm kiếm theo payer type