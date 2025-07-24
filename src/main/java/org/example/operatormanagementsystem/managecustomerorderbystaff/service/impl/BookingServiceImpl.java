package org.example.operatormanagementsystem.managecustomerorderbystaff.service.impl;

import org.example.operatormanagementsystem.ManageHungBranch.dto.response.BookingDetailResponse;
import org.example.operatormanagementsystem.ManageHungBranch.dto.response.SlotsInfoResponse;
import org.example.operatormanagementsystem.ManageHungBranch.repository.StorageUnitRepository;
import org.example.operatormanagementsystem.entity.Booking;
import org.example.operatormanagementsystem.entity.Customer;
import org.example.operatormanagementsystem.entity.StorageUnit;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.enumeration.PaymentStatus;
import org.example.operatormanagementsystem.managecustomerorderbystaff.dto.request.BookingRequest;
import org.example.operatormanagementsystem.managecustomerorderbystaff.repository.BookingRepository;
import org.example.operatormanagementsystem.managecustomerorderbystaff.repository.CustomerRepository;
import org.example.operatormanagementsystem.managestaff_yen.repository.OperatorStaffRepository;
import org.example.operatormanagementsystem.managestaff_yen.repository.UsersRepository; // Import UsersRepository
import org.example.operatormanagementsystem.managecustomerorderbystaff.service.BookingService;
import org.example.operatormanagementsystem.transportunit.repository.TransportUnitRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final UsersRepository usersRepository; // Thêm UsersRepository
    private final StorageUnitRepository storageUnitRepository;
    private final TransportUnitRepository transportUnitRepository;
    private final OperatorStaffRepository operatorStaffRepository;



    @Override
    public long getTotalBookings() {
        return bookingRepository.count();
    }

    @Override
    public double getTotalPaidAmount() {
        return bookingRepository.findAll().stream()
                .filter(b -> "Paid".equalsIgnoreCase(b.getStatus()))
                .mapToDouble(b -> 100.0) // Replace with actual amount calculation
                .sum();
    }

    @Override
    public long getPaidBookingsCount() {
        return bookingRepository.countByStatus("Paid");
    }

    @Override
    public long getUnpaidBookingsCount() {
        return bookingRepository.countByStatusNot("Paid");
    }

    @Override
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    public Optional<Booking> getBookingById(Integer id) {
        return bookingRepository.findById(id);
    }

    @Override
    public Booking updateBooking(Integer id, BookingRequest bookingUpdatesRequest) {
        Booking existingBooking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));

        if (bookingUpdatesRequest.getStatus() != null) {
            existingBooking.setStatus(bookingUpdatesRequest.getStatus());
        }
        if (bookingUpdatesRequest.getDeliveryDate() != null) {
            existingBooking.setDeliveryDate(bookingUpdatesRequest.getDeliveryDate());
        }
        if (bookingUpdatesRequest.getNote() != null) {
            existingBooking.setNote(bookingUpdatesRequest.getNote());
        }

        if (bookingUpdatesRequest.getCustomerId() != null) {
            Customer customer = customerRepository.findById(bookingUpdatesRequest.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found for update with ID: " + bookingUpdatesRequest.getCustomerId()));
            existingBooking.setCustomer(customer);
            if (bookingUpdatesRequest.getCustomerFullName() != null && !bookingUpdatesRequest.getCustomerFullName().isEmpty()) {
                Users user = customer.getUsers();
                if (user != null) {
                    user.setFullName(bookingUpdatesRequest.getCustomerFullName());
                    usersRepository.save(user); // Sử dụng usersRepository đã inject
                }
            }
            if (bookingUpdatesRequest.getSlotIndex() != null
                    && !bookingUpdatesRequest.getSlotIndex().equals(existingBooking.getSlotIndex())) {
                int idx = bookingUpdatesRequest.getSlotIndex();
                if (idx < 0 || idx >= existingBooking.getStorageUnit().getSlotCount())
                    throw new RuntimeException("Slot out of range");
                bookingRepository.findByStorageUnit_StorageIdAndSlotIndex(
                                existingBooking.getStorageUnit().getStorageId(), idx)
                        .ifPresent(b2 -> { throw new RuntimeException("Slot already booked"); });
                existingBooking.setSlotIndex(idx);
            }

        }

        if (bookingUpdatesRequest.getTotal() != null) {
            existingBooking.setTotal(bookingUpdatesRequest.getTotal());
        }
        if (bookingUpdatesRequest.getPaymentStatus() != null) {
            try {
                existingBooking.setPaymentStatus(PaymentStatus.valueOf(bookingUpdatesRequest.getPaymentStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid payment status: " + bookingUpdatesRequest.getPaymentStatus());
            }
        }

        return bookingRepository.save(existingBooking);
    }

    @Override
    public List<Booking> searchBookingsByCustomerName(String fullName) {
        return bookingRepository.findByCustomerUsersFullNameContainingIgnoreCase(fullName);
    }

    @Override
    public Booking updatePaymentStatus(Integer id, String status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));
        try {
            booking.setPaymentStatus(PaymentStatus.valueOf(status.toUpperCase()));
            System.out.println("Cập nhật paymentStatus thành: " + status + " cho booking ID: " + id);
            // Không thay đổi status khi paymentStatus là COMPLETED
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid payment status: " + status);
        }
        Booking savedBooking = bookingRepository.save(booking);
        System.out.println("Booking sau khi lưu: " + savedBooking.getPaymentStatus());
        return savedBooking;
    }
    @Override
    public SlotsInfoResponse getSlotsInfo(Integer storageId) { // 2 overdrive cua Hung
        StorageUnit su = storageUnitRepository.findById(storageId)
                .orElseThrow(() -> new RuntimeException("Storage not found: " + storageId));
        List<Integer> booked = bookingRepository.findAllByStorageUnit_StorageId(storageId)
                .stream().map(Booking::getSlotIndex).toList();
        return new SlotsInfoResponse(su.getSlotCount(), booked);
    }

    @Override
    public Optional<BookingDetailResponse> getBookingDetail(Integer storageId, Integer slotIndex) {
        return bookingRepository.findByStorageUnit_StorageIdAndSlotIndex(storageId, slotIndex)
                .map(b -> {
                    BookingDetailResponse dto = new BookingDetailResponse();
                    // copy từ convertToBookingResponse
                    dto.setBookingId(b.getBookingId());
                    dto.setStatus(b.getStatus());
                    dto.setCreatedAt(b.getCreatedAt());
                    dto.setDeliveryDate(b.getDeliveryDate());
                    dto.setNote(b.getNote());
                    dto.setCustomerId(b.getCustomer().getCustomerId());
                    dto.setCustomerFullName(b.getCustomer().getUsers().getFullName());
                    dto.setTotal(b.getTotal());
                    dto.setPaymentStatus(b.getPaymentStatus().name());
                    // thêm slotIndex
                    dto.setSlotIndex(b.getSlotIndex());
                    return dto;
                });
    }
    @Override
    public BookingDetailResponse createBooking(BookingRequest req) {
        // 1) Load storage và kiểm slotIndex
        StorageUnit su = storageUnitRepository.findById(req.getStorageUnitId())
                .orElseThrow(() -> new RuntimeException("Storage not found: " + req.getStorageUnitId()));
        if (req.getSlotIndex() < 0 || req.getSlotIndex() >= su.getSlotCount()) {
            throw new RuntimeException("Slot index out of range");
        }
        // 2) Kiểm slot chưa booked
        bookingRepository.findByStorageUnit_StorageIdAndSlotIndex(req.getStorageUnitId(), req.getSlotIndex())
                .ifPresent(b -> { throw new RuntimeException("Slot already booked"); });
        // 3) Map và save
        Booking b = new Booking();
        b.setStorageUnit(su);
        b.setSlotIndex(req.getSlotIndex());
        b.setCustomer(customerRepository.findById(req.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found: " + req.getCustomerId())));
        b.setTransportUnit(transportUnitRepository.findById(req.getTransportUnitId())
                .orElseThrow(() -> new RuntimeException("Transport not found: " + req.getTransportUnitId())));
        b.setOperatorStaff(operatorStaffRepository.findById(req.getOperatorStaffId())
                .orElseThrow(() -> new RuntimeException("Operator not found: " + req.getOperatorStaffId())));
        b.setStatus(req.getStatus());
        b.setDeliveryDate(req.getDeliveryDate());
        b.setNote(req.getNote());
        b.setTotal(req.getTotal());
        b.setPaymentStatus(PaymentStatus.valueOf(req.getPaymentStatus().toUpperCase()));
        Booking saved = bookingRepository.save(b);
        // 4) Map to response DTO
        BookingDetailResponse dto = new BookingDetailResponse();
        // copy fields including slotIndex...
        dto.setBookingId(saved.getBookingId());
        dto.setSlotIndex(saved.getSlotIndex());
        dto.setCustomerId(saved.getCustomer().getCustomerId());
        dto.setCustomerFullName(saved.getCustomer().getUsers().getFullName());
        dto.setDeliveryDate(saved.getDeliveryDate());
        dto.setNote(saved.getNote());
        dto.setTotal(saved.getTotal());
        dto.setPaymentStatus(saved.getPaymentStatus().name());
        dto.setStatus(saved.getStatus());
        dto.setCreatedAt(saved.getCreatedAt());
        return dto;
    }
    @Override
    public void deleteBooking(Integer id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking với ID: " + id));
        bookingRepository.delete(booking);
    }

    @Override
    public Map<String, List<Integer>> getAllRelatedIds() {
        Map<String, List<Integer>> ids = new java.util.HashMap<>();
        // Chỉ lấy id, không lấy object (đúng nhu cầu FE)
        ids.put("customerIds", customerRepository.findAll().stream().map(c -> c.getCustomerId()).toList());
        ids.put("operatorStaffIds", operatorStaffRepository.findAll().stream().map(o -> o.getOperatorId()).toList());
        ids.put("transportUnitIds", transportUnitRepository.findAll().stream().map(t -> t.getTransportId()).toList());
        ids.put("storageUnitIds", storageUnitRepository.findAll().stream().map(s -> s.getStorageId()).toList());
        return ids;
    }


}