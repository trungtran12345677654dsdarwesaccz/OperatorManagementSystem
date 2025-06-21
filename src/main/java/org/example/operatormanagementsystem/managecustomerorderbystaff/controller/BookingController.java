package org.example.operatormanagementsystem.managecustomerorderbystaff.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.entity.Booking;
import org.example.operatormanagementsystem.managecustomerorderbystaff.dto.request.BookingRequest;
import org.example.operatormanagementsystem.managecustomerorderbystaff.dto.response.BookingResponse;
import org.example.operatormanagementsystem.managecustomerorderbystaff.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.example.operatormanagementsystem.enumeration.PaymentStatus;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@CrossOrigin(
        origins = {"http://localhost:5173", "http://localhost:3000"},
        allowCredentials = "true",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}
)
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/overview")
    @PreAuthorize("hasRole('ROLE_STAFF')")
    public ResponseEntity<Map<String, Object>> getOverview() {
        try {
            Map<String, Object> overview = Map.of(
                    "totalBookings", bookingService.getTotalBookings(),
                    "totalPaidAmount", bookingService.getTotalPaidAmount(),
                    "paidBookingsCount", bookingService.getPaidBookingsCount(),
                    "unpaidBookingsCount", bookingService.getUnpaidBookingsCount()
            );
            return ResponseEntity.ok(overview);
        } catch (Exception e) {
            e.printStackTrace(); // log lỗi
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "Lỗi khi lấy thông tin tổng quan: " + e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_STAFF')")
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        try {
            List<Booking> bookings = bookingService.getAllBookings();
            List<BookingResponse> responses = bookings.stream()
                    .map(this::convertToBookingResponse)
                    .collect(Collectors.toList());

            return responses.isEmpty()
                    ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                    : ResponseEntity.ok(responses);
        } catch (Exception e) {
            e.printStackTrace(); // log lỗi
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_STAFF')")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Integer id) {
        try {
            return bookingService.getBookingById(id)
                    .map(this::convertToBookingResponse)
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new RuntimeException("Đơn hàng không tìm thấy với ID: " + id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            e.printStackTrace(); // log lỗi
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_STAFF')")
    public ResponseEntity<BookingResponse> updateBooking(@PathVariable Integer id, @Valid @RequestBody BookingRequest bookingRequest) {
        try {
            Booking updated = bookingService.updateBooking(id, bookingRequest);
            return ResponseEntity.ok(convertToBookingResponse(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            e.printStackTrace(); // log lỗi
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ROLE_STAFF')")
    public ResponseEntity<List<BookingResponse>> searchBookings(@RequestParam String fullName) {
        try {
            List<Booking> bookings = bookingService.searchBookingsByCustomerName(fullName);
            List<BookingResponse> responses = bookings.stream()
                    .map(this::convertToBookingResponse)
                    .collect(Collectors.toList());
            return responses.isEmpty()
                    ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                    : ResponseEntity.ok(responses);
        } catch (Exception e) {
            e.printStackTrace(); // log lỗi
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    @PutMapping("/{id}/payment")
    @PreAuthorize("hasRole('ROLE_STAFF')")
    public ResponseEntity<String> updatePaymentStatus(@PathVariable Integer id, @RequestParam String status) {
        try {
            // Convert string to enum safely (case-insensitive)
            PaymentStatus paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
            bookingService.updatePaymentStatus(id, paymentStatus.name());
            return ResponseEntity.ok("Cập nhật trạng thái thanh toán thành công!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Trạng thái thanh toán không hợp lệ. Chỉ chấp nhận: COMPLETED, INCOMPLETED.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi cập nhật trạng thái thanh toán: " + e.getMessage());
        }
    }

    private BookingResponse convertToBookingResponse(Booking booking) {
        if (booking == null) return null;

        BookingResponse response = new BookingResponse();
        response.setBookingId(booking.getBookingId());
        response.setStatus(booking.getStatus());
        response.setCreatedAt(booking.getCreatedAt());
        response.setDeliveryDate(booking.getDeliveryDate());
        response.setNote(booking.getNote());
        response.setTotal(booking.getTotal()); // Thêm total
        response.setPaymentStatus(booking.getPaymentStatus() != null ? booking.getPaymentStatus().name() : null); // Thêm paymentStatus

        if (booking.getCustomer() != null) {
            var user = booking.getCustomer().getUsers();
            if (user != null) {
                response.setCustomerId(user.getId());
                response.setCustomerFullName(user.getFullName());
            } else {
                response.setCustomerFullName("Không rõ người dùng");
            }
        } else {
            response.setCustomerFullName("Không có khách hàng");
        }

        return response;
    }
}
