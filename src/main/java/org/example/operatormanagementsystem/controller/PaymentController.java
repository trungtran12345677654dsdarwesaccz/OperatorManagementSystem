package org.example.operatormanagementsystem.controller;

import org.example.operatormanagementsystem.entity.Payment;
import org.example.operatormanagementsystem.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    @Autowired
    private PaymentService service;

    @GetMapping
    public List<Payment> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Payment getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public Payment add(@RequestBody Payment payment) {
        return service.add(payment);
    }

    @PutMapping("/{id}")
    public Payment update(@PathVariable Long id, @RequestBody Payment payment) {
        return service.update(id, payment);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/search")
    public List<Payment> search(
            @RequestParam String customerName,
            @RequestParam String note,
            @RequestParam String status
    ) {
        return service.search(customerName, note, status);
    }
}
