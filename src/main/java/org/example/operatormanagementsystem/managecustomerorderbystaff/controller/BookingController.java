package org.example.operatormanagementsystem.managecustomerorderbystaff.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.operatormanagementsystem.entity.Booking;
import org.example.operatormanagementsystem.managecustomerorderbystaff.service.BookingServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Manage Order", description = "APIs for managing customer receipts and payments")
public class BookingController {
    @Autowired
    private BookingServiceImpl bookingService;

    // Lấy thông tin tổng quan về đơn hàng
    @GetMapping("/overview")
    public Map<String, Object> getOverview() {
        return Map.of(
                "totalBookings", bookingService.getTotalBookings(),
                "totalPaidAmount", bookingService.getTotalPaidAmount(),
                "paidBookingsCount", bookingService.getPaidBookingsCount(),
                "unpaidBookingsCount", bookingService.getUnpaidBookingsCount()
        );
    }

    // Lấy danh sách tất cả các booking
    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingService.getAllBookings();
    }

    // Lấy thông tin booking theo ID
    @GetMapping("/{id}")
    public Booking getBookingById(@PathVariable Integer id) {
        return bookingService.getBookingById(id).orElseThrow(() -> new RuntimeException("Booking không tìm thấy"));
    }

    // Tạo mới một booking
    @PostMapping
    public Booking createBooking(@RequestBody Booking booking) {
        return bookingService.saveBooking(booking);
    }

    // Cập nhật thông tin booking
    @PutMapping("/{id}")
    public Booking updateBooking(@PathVariable Integer id, @RequestBody Booking booking) {
        return bookingService.updateBooking(id, booking);
    }

    // Xóa một booking theo ID
    @DeleteMapping("/{id}")
    public void deleteBooking(@PathVariable Integer id) {
        bookingService.deleteBooking(id);
    }

    // Tìm kiếm booking theo tên khách hàng
    @GetMapping("/search")
    public List<Booking> searchBookings(@RequestParam String fullName) {
        return bookingService.searchBookingsByCustomerName(fullName);
    }

    // Cập nhật trạng thái thanh toán của booking
    @PutMapping("/{id}/payment")
    public Booking updatePaymentStatus(@PathVariable Integer id, @RequestParam String status) {
        return bookingService.updatePaymentStatus(id, status);
    }
}