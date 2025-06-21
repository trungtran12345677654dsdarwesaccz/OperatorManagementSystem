// src/main/java/org/example/operatormanagementsystem/managecustomerorderbystaff/controller/BookingController.java
package org.example.operatormanagementsystem.managecustomerorderbystaff.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.entity.Booking; // Giữ lại nếu convertToBookingResponse cần
// import org.example.operatormanagementsystem.entity.Customer; // Có thể bỏ nếu không dùng trực tiếp
// import org.example.operatormanagementsystem.entity.OperatorStaff; // Có thể bỏ nếu không dùng trực tiếp
// import org.example.operatormanagementsystem.entity.Users; // Có thể bỏ nếu không dùng trực tiếp
import org.example.operatormanagementsystem.managecustomerorderbystaff.dto.request.BookingRequest; // Cần cho update
import org.example.operatormanagementsystem.managecustomerorderbystaff.dto.response.BookingResponse;
import org.example.operatormanagementsystem.managecustomerorderbystaff.service.BookingService;
// Loại bỏ các repository không còn cần thiết cho View và Update Booking:
// import org.example.operatormanagementsystem.managecustomerorderbystaff.repository.StorageUnitRepository;
// import org.example.operatormanagementsystem.managecustomerorderbystaff.repository.UsersRepository;
// import org.example.operatormanagementsystem.transportunit.repository.TransportUnitRepository;
// import org.example.operatormanagementsystem.managecustomerorderbystaff.repository.CustomerRepository; // Có thể bỏ
// import org.example.operatormanagementsystem.repository.OperatorStaffRepository; // Có thể bỏ

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    // Loại bỏ các injections không còn cần thiết sau khi bỏ convertToBookingEntity cho POST:
    // private final UsersRepository usersRepository;
    // private final StorageUnitRepository storageUnitRepository;
    // private final TransportUnitRepository transportUnitRepository;
    // private final CustomerRepository customerRepository;
    // private final OperatorStaffRepository operatorStaffRepository;

    // Lấy thông tin tổng quan về đơn hàng
    @GetMapping("/overview")
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Lỗi khi lấy thông tin tổng quan: " + e.getMessage()));
        }
    }

    // Lấy danh sách tất cả các đơn hàng
    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        try {
            List<Booking> bookings = bookingService.getAllBookings();
            List<BookingResponse> bookingResponses = bookings.stream()
                    .map(this::convertToBookingResponse)
                    .collect(Collectors.toList());
            return bookingResponses.isEmpty() ? ResponseEntity.status(HttpStatus.NO_CONTENT).build() : ResponseEntity.ok(bookingResponses);
        } catch (Exception e) {
            BookingResponse errorResponse = new BookingResponse();
            errorResponse.setErrorMessage("Lỗi khi lấy danh sách đơn hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of(errorResponse));
        }
    }

    // Lấy thông tin đơn hàng theo ID
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Integer id) {
        try {
            return bookingService.getBookingById(id)
                    .map(this::convertToBookingResponse)
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new RuntimeException("Đơn hàng không tìm thấy với ID: " + id));
        } catch (RuntimeException e) {
            BookingResponse errorResponse = new BookingResponse();
            errorResponse.setErrorMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            BookingResponse errorResponse = new BookingResponse();
            errorResponse.setErrorMessage("Lỗi khi lấy thông tin đơn hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Tạo mới một đơn hàng (Đã comment out)
    //    @PostMapping
    //    public ResponseEntity<String> createBooking(@Valid @RequestBody BookingRequest bookingRequest) {
    //        try {
    //            Booking booking = convertToBookingEntity(bookingRequest);
    //            bookingService.saveBooking(booking);
    //            return ResponseEntity.status(HttpStatus.CREATED).body("Tạo đơn hàng thành công!");
    //        } catch (RuntimeException e) {
    //            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    //        } catch (Exception e) {
    //            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Không thể tạo đơn hàng: " + e.getMessage());
    //        }
    //    }

    // Cập nhật thông tin đơn hàng
    @PutMapping("/{id}")
    public ResponseEntity<BookingResponse> updateBooking(@PathVariable Integer id, @Valid @RequestBody BookingRequest bookingRequest) {
        try {
            Booking updatedBooking = bookingService.updateBooking(id, bookingRequest);
            return ResponseEntity.ok(convertToBookingResponse(updatedBooking));
        } catch (RuntimeException e) {
            BookingResponse errorResponse = new BookingResponse();
            errorResponse.setErrorMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            BookingResponse errorResponse = new BookingResponse();
            errorResponse.setErrorMessage("Không thể cập nhật đơn hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Xóa một đơn hàng theo ID (Đã comment out)
    //    @DeleteMapping("/{id}")
    //    public ResponseEntity<String> deleteBooking(@PathVariable Integer id) {
    //        try {
    //            bookingService.deleteBooking(id);
    //            return ResponseEntity.ok("Xóa đơn hàng thành công!");
    //        } catch (RuntimeException e) {
    //            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    //        } catch (Exception e) {
    //            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Không thể xóa đơn hàng: " + e.getMessage());
    //        }
    //    }

    // Tìm kiếm đơn hàng theo tên khách hàng
    @GetMapping("/search")
    public ResponseEntity<List<BookingResponse>> searchBookings(@RequestParam String fullName) {
        try {
            List<Booking> bookings = bookingService.searchBookingsByCustomerName(fullName);
            List<BookingResponse> bookingResponses = bookings.stream()
                    .map(this::convertToBookingResponse)
                    .collect(Collectors.toList());
            return bookingResponses.isEmpty() ? ResponseEntity.status(HttpStatus.NO_CONTENT).build() : ResponseEntity.ok(bookingResponses);
        } catch (Exception e) {
            BookingResponse errorResponse = new BookingResponse();
            errorResponse.setErrorMessage("Lỗi khi tìm kiếm đơn hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of(errorResponse));
        }
    }

    // Cập nhật trạng thái thanh toán của đơn hàng
    @PutMapping("/{id}/payment")
    public ResponseEntity<String> updatePaymentStatus(@PathVariable Integer id, @RequestParam String status) {
        try {
            bookingService.updatePaymentStatus(id, status);
            return ResponseEntity.ok("Cập nhật trạng thái thanh toán thành công!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Không thể cập nhật trạng thái thanh toán: " + e.getMessage());
        }
    }

    // --- Phương thức hỗ trợ ---
    private BookingResponse convertToBookingResponse(Booking booking) {
        if (booking == null) {
            return null;
        }
        BookingResponse response = new BookingResponse();
        response.setBookingId(booking.getBookingId());
        response.setStatus(booking.getStatus());
        response.setCreatedAt(booking.getCreatedAt());
        response.setDeliveryDate(booking.getDeliveryDate());
        response.setNote(booking.getNote());

        // Chỉ giữ thông tin Customer
        if (booking.getCustomer() != null && booking.getCustomer().getUsers() != null) {
            response.setCustomerId(booking.getCustomer().getUsers().getId());
            response.setCustomerFullName(booking.getCustomer().getUsers().getFullName());
        }

        // Loại bỏ tham chiếu đến StorageUnit, TransportUnit, OperatorStaff
        return response;
    }

    // Xóa hoàn toàn phương thức convertToBookingEntity nếu không dùng cho tạo mới nữa
    // Nếu bạn muốn dùng nó để ánh xạ từ DTO sang Entity cho việc cập nhật các mối quan hệ phức tạp,
    // thì bạn sẽ cần điều chỉnh nó để chỉ cập nhật các trường được cung cấp và tìm kiếm các entity con
    // từ database thay vì tạo mới.
    // Tuy nhiên, cách làm ở BookingServiceImpl.java đã đủ cho việc cập nhật cơ bản.
    // private Booking convertToBookingEntity(BookingRequest request) { ... }
}