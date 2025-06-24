package org.example.operatormanagementsystem.customer_thai.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.customer_thai.dto.request.CreateBookingRequest;
import org.example.operatormanagementsystem.customer_thai.dto.response.BookingCustomerResponse;
import org.example.operatormanagementsystem.customer_thai.repository.BookingCustomerRepository;
import org.example.operatormanagementsystem.customer_thai.repository.OperatorStaffRepository;
import org.example.operatormanagementsystem.customer_thai.repository.StorageUnitRepository;
import org.example.operatormanagementsystem.customer_thai.service.BookingCustomerService;
import org.example.operatormanagementsystem.customer_thai.service.CustomerInfoService;
import org.example.operatormanagementsystem.entity.*;
import org.example.operatormanagementsystem.enumeration.PaymentStatus;
import org.example.operatormanagementsystem.transportunit.repository.TransportUnitRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service("bookingServiceImpl_thai")
@RequiredArgsConstructor
public class BookingCustomerServiceImpl implements BookingCustomerService {

    @Qualifier("bookingRepository_thai")
    private final BookingCustomerRepository bookingCustomerRepository;
    private final CustomerInfoService customerInfoService;
    @Qualifier("storageUnitRepository_thai")
    private final StorageUnitRepository storageUnitRepository;
    private final TransportUnitRepository transportUnitRepository;
    @Qualifier("operatorStaffRepository_thai")
    private final OperatorStaffRepository operatorStaffRepository;

    @Override
    @Transactional
    public BookingCustomerResponse createBooking(CreateBookingRequest request) {
        Users currentUser = customerInfoService.getCurrentCustomerUser();
        if (currentUser.getCustomer() == null) {
            throw new RuntimeException("Could not find customer profile for the current user.");
        }

        StorageUnit storageUnit = storageUnitRepository.findById(request.getStorageId())
                .orElseThrow(() -> new RuntimeException("Storage unit not found"));
        TransportUnit transportUnit = transportUnitRepository.findById(request.getTransportId())
                .orElseThrow(() -> new RuntimeException("Transport unit not found"));
        OperatorStaff operatorStaff = operatorStaffRepository.findById(request.getOperatorId())
                .orElseThrow(() -> new RuntimeException("Operator staff not found"));

        Booking booking = Booking.builder()
                .customer(currentUser.getCustomer())
                .storageUnit(storageUnit)
                .transportUnit(transportUnit)
                .operatorStaff(operatorStaff)
                .pickupLocation(request.getPickupLocation())
                .deliveryLocation(request.getDeliveryLocation())
                .status("PENDING")
                .paymentStatus(PaymentStatus.INCOMPLETED)
                .deliveryDate(request.getDeliveryDate())
                .note(request.getNote())
                .total(request.getTotal())
                .build();

        Booking savedBooking = bookingCustomerRepository.save(booking);
        return mapToBookingResponse(savedBooking);
    }

    @Override
    public List<BookingCustomerResponse> getAllMyBookings() {
        Users currentUser = customerInfoService.getCurrentCustomerUser();
        if (currentUser.getCustomer() == null) {
            throw new RuntimeException("Could not find customer profile for the current user.");
        }
        Integer customerId = currentUser.getCustomer().getCustomerId();

        return bookingCustomerRepository.findByCustomer_CustomerId(customerId)
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BookingCustomerResponse getBookingById(Integer bookingId) {
        Users currentUser = customerInfoService.getCurrentCustomerUser();
        if (currentUser.getCustomer() == null) {
            throw new RuntimeException("Could not find customer profile for the current user.");
        }
        Integer customerId = currentUser.getCustomer().getCustomerId();

        Booking booking = bookingCustomerRepository.findByBookingIdAndCustomer_CustomerId(bookingId, customerId)
                .orElseThrow(() -> new RuntimeException("Booking not found or you do not have permission to view it."));
        return mapToBookingResponse(booking);
    }

    @Override
    @Transactional
    public void deleteBooking(Integer bookingId) {
        Users currentUser = customerInfoService.getCurrentCustomerUser();
        if (currentUser.getCustomer() == null) {
            throw new RuntimeException("Could not find customer profile for the current user.");
        }
        Integer customerId = currentUser.getCustomer().getCustomerId();

        Booking booking = bookingCustomerRepository.findByBookingIdAndCustomer_CustomerId(bookingId, customerId)
                .orElseThrow(() -> new RuntimeException("Booking not found or you do not have permission to delete it."));

        if (!"PENDING".equalsIgnoreCase(booking.getStatus())) {
            throw new RuntimeException("Cannot delete a booking that is not in PENDING status.");
        }

        bookingCustomerRepository.delete(booking);
    }

    private BookingCustomerResponse mapToBookingResponse(Booking booking) {
        return BookingCustomerResponse.builder()
                .bookingId(booking.getBookingId())
                .customerId(booking.getCustomer().getCustomerId())
                .customerName(booking.getCustomer().getUsers().getFullName())
                .storageId(booking.getStorageUnit().getStorageId())
                .storageName(booking.getStorageUnit().getName())
                .transportId(booking.getTransportUnit().getTransportId())
                .transportName(booking.getTransportUnit().getNameCompany())
                .operatorId(booking.getOperatorStaff().getOperatorId())
                .operatorName(booking.getOperatorStaff().getUsers().getFullName())
                .pickupLocation(booking.getPickupLocation())
                .deliveryLocation(booking.getDeliveryLocation())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .deliveryDate(booking.getDeliveryDate())
                .note(booking.getNote())
                .total(booking.getTotal())
                .build();
    }
} 