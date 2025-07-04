package org.example.operatormanagementsystem.managecustomerorderbystaff.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.ManageHungBranch.dto.response.BookingDetailResponse;
import org.example.operatormanagementsystem.ManageHungBranch.dto.response.SlotsInfoResponse;
import org.example.operatormanagementsystem.entity.Booking;
import org.example.operatormanagementsystem.managecustomerorderbystaff.dto.request.BookingRequest;
import org.example.operatormanagementsystem.managecustomerorderbystaff.dto.response.BookingResponse;
import org.example.operatormanagementsystem.managecustomerorderbystaff.service.BookingService;
import org.example.operatormanagementsystem.enumeration.PaymentStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
            // Chuyển trạng thái tiếng Việt sang tiếng Anh trước khi cập nhật
            String englishStatus = toEnglishStatus(bookingRequest.getStatus());
            BookingRequest modifiedRequest = new BookingRequest();
            modifiedRequest.setStatus(englishStatus);
            modifiedRequest.setDeliveryDate(bookingRequest.getDeliveryDate());
            modifiedRequest.setNote(bookingRequest.getNote());
            modifiedRequest.setCustomerId(bookingRequest.getCustomerId());
            modifiedRequest.setCustomerFullName(bookingRequest.getCustomerFullName());
            modifiedRequest.setStorageUnitId(bookingRequest.getStorageUnitId());
            modifiedRequest.setTransportUnitId(bookingRequest.getTransportUnitId());
            modifiedRequest.setOperatorStaffId(bookingRequest.getOperatorStaffId());
            modifiedRequest.setTotal(bookingRequest.getTotal());
            modifiedRequest.setPaymentStatus(bookingRequest.getPaymentStatus());

            Booking updated = bookingService.updateBooking(id, modifiedRequest);
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
    public ResponseEntity<BookingResponse> updatePaymentStatus(@PathVariable Integer id, @RequestParam String status) {
        try {
            // Convert string to enum safely (case-insensitive)
            PaymentStatus paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
            Booking updatedBooking = bookingService.updatePaymentStatus(id, paymentStatus.name()); // Cập nhật và lấy Booking
            return ResponseEntity.ok(convertToBookingResponse(updatedBooking));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new BookingResponse()); // Trả về lỗi nếu cần
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new BookingResponse()); // Trả về lỗi nếu không tìm thấy
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BookingResponse()); // Trả về lỗi nếu có ngoại lệ
        }
    }

    private String toEnglishStatus(String status) {
        if (status == null) return null;
        if ("Đang xử lý".equalsIgnoreCase(status) || "đang xử lý".equalsIgnoreCase(status)) {
            return "PENDING";
        } else if ("Đang giao".equalsIgnoreCase(status) || "đang giao".equalsIgnoreCase(status)) {
            return "SHIPPING";
        } else if ("Hoàn thành".equalsIgnoreCase(status) || "hoàn thành".equalsIgnoreCase(status)) {
            return "COMPLETED";
        } else if ("Hủy".equalsIgnoreCase(status) || "hủy".equalsIgnoreCase(status)) {
            return "CANCELED";
        }
        return status.toUpperCase(); // Nếu không khớp, giữ nguyên giá trị (giả sử là tiếng Anh)
    }

    private String toVietnameseStatus(String status) {
        if (status == null) return null;
        if ("PENDING".equalsIgnoreCase(status)) {
            return "Đang xử lý";
        } else if ("SHIPPING".equalsIgnoreCase(status)) {
            return "Đang giao";
        } else if ("COMPLETED".equalsIgnoreCase(status)) {
            return "Hoàn thành";
        } else if ("CANCELED".equalsIgnoreCase(status)) {
            return "Hủy";
        }
        return status; // Nếu không khớp, giữ nguyên giá trị
    }

    private BookingResponse convertToBookingResponse(Booking booking) {
        if (booking == null) return null;
        BookingResponse response = new BookingResponse();
        response.setBookingId(booking.getBookingId());
        // Chuyển trạng thái sang tiếng Việt
        response.setStatus(toVietnameseStatus(booking.getStatus()));
        response.setCreatedAt(booking.getCreatedAt());
        response.setDeliveryDate(booking.getDeliveryDate());
        response.setNote(booking.getNote());
        response.setTotal(booking.getTotal());
        response.setPaymentStatus(booking.getPaymentStatus() != null ? booking.getPaymentStatus().name() : null);
        if (booking.getCustomer() != null) {
            var user = booking.getCustomer().getUsers();
            response.setCustomerId(user != null ? user.getId() : null);
            response.setCustomerFullName(user != null ? user.getFullName() : "Không rõ người dùng");
        } else {
            response.setCustomerFullName("Không có khách hàng");
        }
        return response;
    }

    // 1) Lấy slotCount + danh sách ô đã full cho kho
    @GetMapping("/storage/{storageId}/slots")
    @PreAuthorize("hasRole('ROLE_STAFF')")
    public ResponseEntity<SlotsInfoResponse> getSlotsInfo(@PathVariable Integer storageId) {
        return ResponseEntity.ok(bookingService.getSlotsInfo(storageId));
    }

    // 2) Lấy chi tiết booking cho một ô cụ thể
    @GetMapping("/storage/{storageId}/slots/{slotIndex}")
    @PreAuthorize("hasRole('ROLE_STAFF')")
    public ResponseEntity<BookingDetailResponse> getBookingDetail(
            @PathVariable Integer storageId,
            @PathVariable Integer slotIndex) {
        return bookingService.getBookingDetail(storageId, slotIndex)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_STAFF')")
    public ResponseEntity<BookingDetailResponse> createBooking(
            @Valid @RequestBody BookingRequest req) {
        BookingDetailResponse dto = bookingService.createBooking(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_STAFF')")
    public ResponseEntity<Void> deleteBooking(@PathVariable Integer id) {
            bookingService.deleteBooking(id);
            return ResponseEntity.noContent().build();
        }




}
