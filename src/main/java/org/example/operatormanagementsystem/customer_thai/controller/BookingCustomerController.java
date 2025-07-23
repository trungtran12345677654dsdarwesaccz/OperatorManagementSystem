package org.example.operatormanagementsystem.customer_thai.controller;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.customer_thai.dto.request.CreateBookingRequest;
import org.example.operatormanagementsystem.customer_thai.dto.request.ItemsRequest;
import org.example.operatormanagementsystem.customer_thai.dto.response.BookingCustomerResponse;
import org.example.operatormanagementsystem.customer_thai.dto.response.ItemsResponse;
import org.example.operatormanagementsystem.customer_thai.dto.response.PromotionBookingResponse;
import org.example.operatormanagementsystem.customer_thai.dto.response.SlotStatusResponse;
import org.example.operatormanagementsystem.customer_thai.service.BookingCustomerService;
import org.example.operatormanagementsystem.customer_thai.service.PromotionService;
import org.example.operatormanagementsystem.entity.OperatorStaff;
import org.example.operatormanagementsystem.entity.StorageUnit;
import org.example.operatormanagementsystem.entity.TransportUnit;
import org.example.operatormanagementsystem.customer_thai.repository.OperatorStaffRepository;
import org.example.operatormanagementsystem.customer_thai.repository.StorageUnitRepository;
import org.example.operatormanagementsystem.transportunit.repository.TransportUnitRepository;
import org.example.operatormanagementsystem.enumeration.UserRole;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class    BookingCustomerController {

    private final BookingCustomerService bookingCustomerService;
    
    @Qualifier("storageUnitRepository_thai")
    private final StorageUnitRepository storageUnitRepository;
    
    private final TransportUnitRepository transportUnitRepository;
    
    @Qualifier("operatorStaffRepository_thai")
    private final OperatorStaffRepository operatorStaffRepository;
    
    @Qualifier("promotionService_thai")
    private final PromotionService promotionService;

    @PostMapping("/bookings")
    public ResponseEntity<BookingCustomerResponse> createBooking(@RequestBody CreateBookingRequest request) {
        BookingCustomerResponse response = bookingCustomerService.createBooking(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/bookings/customer")
    public ResponseEntity<List<BookingCustomerResponse>> getBookingsByCustomerId() {
        List<BookingCustomerResponse> bookings = bookingCustomerService.getAllMyBookings();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<BookingCustomerResponse> getBookingById(@PathVariable Integer bookingId) {
        BookingCustomerResponse booking = bookingCustomerService.getBookingById(bookingId);
        return ResponseEntity.ok(booking);
    }

    @DeleteMapping("/bookings/{bookingId}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Integer bookingId) {
        bookingCustomerService.deleteBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    // Endpoint để lấy items của một booking
    @GetMapping("/bookings/{bookingId}/items")
    public ResponseEntity<List<ItemsResponse>> getBookingItems(@PathVariable Integer bookingId) {
        List<ItemsResponse> items = bookingCustomerService.getBookingItems(bookingId);
        return ResponseEntity.ok(items);
    }

    // Endpoint để thêm items vào booking
    @PostMapping("/bookings/{bookingId}/items")
    public ResponseEntity<List<ItemsResponse>> addItemsToBooking(
            @PathVariable Integer bookingId,
            @RequestBody List<ItemsRequest> itemsRequest) {
        List<ItemsResponse> items = bookingCustomerService.addItemsToBooking(bookingId, itemsRequest);
        return ResponseEntity.ok(items);
    }

    // Endpoint để cập nhật một item cụ thể trong booking
    @PutMapping("/bookings/{bookingId}/items/{itemId}")
    public ResponseEntity<ItemsResponse> updateBookingItem(
            @PathVariable Integer bookingId,
            @PathVariable Integer itemId,
            @RequestBody ItemsRequest itemRequest) {
        ItemsResponse item = bookingCustomerService.updateBookingItem(bookingId, itemId, itemRequest);
        return ResponseEntity.ok(item);
    }

    // Endpoint để cập nhật tất cả items của booking
    @PutMapping("/bookings/{bookingId}/items")
    public ResponseEntity<List<ItemsResponse>> updateBookingItems(
            @PathVariable Integer bookingId,
            @RequestBody List<ItemsRequest> itemsRequest) {
        List<ItemsResponse> items = bookingCustomerService.updateBookingItems(bookingId, itemsRequest);
        return ResponseEntity.ok(items);
    }

    // Endpoint để xóa một item cụ thể trong booking
    @DeleteMapping("/bookings/{bookingId}/items/{itemId}")
    public ResponseEntity<Void> deleteBookingItem(@PathVariable Integer bookingId, @PathVariable Integer itemId) {
        bookingCustomerService.deleteBookingItem(bookingId, itemId);
        return ResponseEntity.noContent().build();
    }

    // API cho phép customer hủy booking
    @PatchMapping("/bookings/{bookingId}/cancel")
    public ResponseEntity<BookingCustomerResponse> cancelBooking(@PathVariable Integer bookingId) {
        BookingCustomerResponse response = bookingCustomerService.cancelBooking(bookingId);
        return ResponseEntity.ok(response);
    }

    // New endpoints to get available options for booking
    @GetMapping("/storage-units")
    public ResponseEntity<List<StorageUnitInfo>> getAvailableStorageUnits() {
        List<StorageUnit> storageUnits = storageUnitRepository.findAll();
        List<StorageUnitInfo> storageUnitInfos = storageUnits.stream()
                .map(storage -> StorageUnitInfo.builder()
                        .storageId(storage.getStorageId())
                        .name(storage.getName())
                        .address(storage.getAddress())
                        .phone(storage.getPhone())
                        .status(storage.getStatus())
                        .image(storage.getImage())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(storageUnitInfos);
    }

    @GetMapping("/transport-units")
    public ResponseEntity<List<TransportUnitInfo>> getAvailableTransportUnits() {
        List<TransportUnit> transportUnits = transportUnitRepository.findAll();
        List<TransportUnitInfo> transportUnitInfos = transportUnits.stream()
                .map(transport -> TransportUnitInfo.builder()
                        .transportId(transport.getTransportId())
                        .nameCompany(transport.getNameCompany())
                        .namePersonContact(transport.getNamePersonContact())
                        .phone(transport.getPhone())
                        .licensePlate(transport.getLicensePlate())
                        .status(transport.getStatus())
                        .image(transport.getImageTransportUnit())
                        .capacityPerVehicle(transport.getCapacityPerVehicle())
                        .numberOfVehicles(transport.getNumberOfVehicles())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(transportUnitInfos);
    }

    @GetMapping("/operator-staff")
    public ResponseEntity<List<OperatorStaffInfo>> getAvailableOperatorStaff() {
        // Fetch all staff and filter in memory to avoid changing the repository
        List<OperatorStaff> operatorStaffs = operatorStaffRepository.findAll()
                .stream()
                .filter(staff -> staff.getUsers() != null && staff.getUsers().getRole() == UserRole.STAFF)
                .collect(Collectors.toList());

        List<OperatorStaffInfo> operatorStaffInfos = operatorStaffs.stream()
                .map(staff -> OperatorStaffInfo.builder()
                        .operatorId(staff.getOperatorId())
                        .fullName(staff.getUsers().getFullName())
                        .email(staff.getUsers().getEmail())
                        .phone(staff.getUsers().getPhone())
                        .img(staff.getUsers().getImg())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(operatorStaffInfos);
    }

    @GetMapping("/promotions")
    public ResponseEntity<List<PromotionBookingResponse>> getAvailablePromotions() {
        List<PromotionBookingResponse> promotions = promotionService.getActivePromotions();
        return ResponseEntity.ok(promotions);
    }

    // Endpoint mới: lấy trạng thái slot của một storage
    @GetMapping("/storage-units/{storageId}/slots")
    public ResponseEntity<SlotStatusResponse> getSlotStatusByStorageId(@PathVariable Integer storageId) {
        SlotStatusResponse response = bookingCustomerService.getSlotStatusByStorageId(storageId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transport-units/{transportUnitId}/checkvehicle")
    public ResponseEntity<?> checkVehicleAvailability(
            @PathVariable Integer transportUnitId,
            @RequestParam Integer vehicleQuantity) {
        boolean available = bookingCustomerService.checkVehicleAvailability(transportUnitId, vehicleQuantity);
        if (available) {
            return ResponseEntity.ok(java.util.Map.of("available", true));
        } else {
            return ResponseEntity.ok(java.util.Map.of("available", false, "message", "Không đủ số lượng xe"));
        }
    }

    // DTO classes for the new endpoints
    @lombok.Data
    @lombok.Builder
    public static class StorageUnitInfo {
        private Integer storageId;
        private String name;
        private String address;
        private String phone;
        private String status;
        private String image;
    }

    @lombok.Data
    @lombok.Builder
    public static class TransportUnitInfo {
        private Integer transportId;
        private String nameCompany;
        private String namePersonContact;
        private String phone;
        private String licensePlate;
        private org.example.operatormanagementsystem.enumeration.UserStatus status;
        private String image;
        private Double capacityPerVehicle;
        private Integer numberOfVehicles;
    }

    @lombok.Data
    @lombok.Builder
    public static class OperatorStaffInfo {
        private Integer operatorId;
        private String fullName;
        private String email;
        private String phone;
        private String img;
    }
} 