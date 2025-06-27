package org.example.operatormanagementsystem.customer_thai.controller;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.customer_thai.dto.request.CreateBookingRequest;
import org.example.operatormanagementsystem.customer_thai.dto.response.BookingCustomerResponse;
import org.example.operatormanagementsystem.customer_thai.service.BookingCustomerService;
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

    @PostMapping("/bookings")
    public ResponseEntity<BookingCustomerResponse> createBooking(@RequestBody CreateBookingRequest request) {
        BookingCustomerResponse response = bookingCustomerService.createBooking(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<BookingCustomerResponse>> getAllMyBookings() {
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
                        .image(storage.getImageStorageUnit())
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