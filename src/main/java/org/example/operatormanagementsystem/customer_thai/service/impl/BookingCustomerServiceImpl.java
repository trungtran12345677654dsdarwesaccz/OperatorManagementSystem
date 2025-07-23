package org.example.operatormanagementsystem.customer_thai.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.customer_thai.dto.request.CreateBookingRequest;
import org.example.operatormanagementsystem.customer_thai.dto.response.BookingCustomerResponse;
import org.example.operatormanagementsystem.customer_thai.repository.BookingCustomerRepository;
import org.example.operatormanagementsystem.customer_thai.repository.ItemsRepository;
import org.example.operatormanagementsystem.customer_thai.repository.OperatorStaffRepository;
import org.example.operatormanagementsystem.enumeration.PaymentStatus;
import org.example.operatormanagementsystem.customer_thai.repository.PromotionRepository;
import org.example.operatormanagementsystem.customer_thai.repository.StorageUnitRepository;
import org.example.operatormanagementsystem.customer_thai.service.BookingCustomerService;
import org.example.operatormanagementsystem.customer_thai.service.CustomerInfoService;
import org.example.operatormanagementsystem.entity.*;
import org.example.operatormanagementsystem.transportunit.repository.TransportUnitRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.example.operatormanagementsystem.customer_thai.service.NotificationEventService;
import org.example.operatormanagementsystem.customer_thai.dto.response.SlotStatusResponse;

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
    
    @Qualifier("promotionRepository_thai")
    private final PromotionRepository promotionRepository;

    private final NotificationEventService notificationEventService;
    
    @Qualifier("itemsRepository")
    private final ItemsRepository itemsRepository;

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

        // Tìm promotion nếu có
        Promotion promotion = null;
        if (request.getPromotionName() != null && !request.getPromotionName().trim().isEmpty()) {
            promotion = promotionRepository.findByName(request.getPromotionName())
                    .orElse(null);
        }

        // Tạo booking ban đầu (chưa có note vì bookingId chưa có)
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
                .note(null) // tạm để null
                .total(request.getTotal())
                .promotion(promotion)
                .homeType(request.getHomeType())
                .slotIndex(request.getSlotIndex())
                .vehicleQuantity(request.getVehicleQuantity())
                .build();

        Booking savedBooking = bookingCustomerRepository.save(booking);

        if (savedBooking.getNote() == null || savedBooking.getNote().isBlank()) {
            String note = "BOOKING" + savedBooking.getBookingId();
            savedBooking.setNote(note);
            bookingCustomerRepository.save(savedBooking); // update lại note
        }


        // Xử lý items nếu có
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            java.util.            List<Items> itemsList = request.getItems().stream()
                    .map(itemRequest -> Items.builder()
                            .name(itemRequest.getName())
                            .quantity(itemRequest.getQuantity())
                            .weight(itemRequest.getWeight())
                            .volume(itemRequest.getVolume())
                            .modular(itemRequest.getModular())
                            .bulky(itemRequest.getBulky())
                            .room(itemRequest.getRoom())
                            .booking(savedBooking)
                            .build())
                    .collect(Collectors.toList());
            
            // Lưu items vào database
            itemsRepository.saveAll(itemsList);
        }
        
        // Sau khi tạo booking thành công, tạo notification
        Customer customer = savedBooking.getCustomer();
        
        notificationEventService.createBookingStatusNotification(
            customer, 
            savedBooking.getBookingId().toString(), 
            "N/A", 
            savedBooking.getStatus()
        );
        
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
    
        if (!"PENDING".equals(booking.getStatus())) {
            throw new RuntimeException("Cannot delete a booking that is not in PENDING status.");
        }
    
        // Lưu thông tin customer và booking ID trước khi xóa
        Customer customer = booking.getCustomer();
        String bookingIdStr = booking.getBookingId().toString();
    
        // Xóa booking
        bookingCustomerRepository.delete(booking);
    
        // Tạo notification sau khi xóa booking thành công
        try {
            System.out.println("Creating notification for booking: " + bookingIdStr);
            notificationEventService.createBookingDeletedNotification(
                customer, 
                bookingIdStr
            );
            System.out.println("Notification created successfully");
        } catch (Exception e) {
            System.err.println("Error creating notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    @Transactional
    public BookingCustomerResponse cancelBooking(Integer bookingId) {
        Users currentUser = customerInfoService.getCurrentCustomerUser();
        if (currentUser.getCustomer() == null) {
            throw new RuntimeException("Could not find customer profile for the current user.");
        }
        Integer customerId = currentUser.getCustomer().getCustomerId();
        Booking booking = bookingCustomerRepository.findByBookingIdAndCustomer_CustomerId(bookingId, customerId)
                .orElseThrow(() -> new RuntimeException("Booking not found or you do not have permission to cancel it."));
        if ("CANCELED".equals(booking.getStatus())) {
            throw new RuntimeException("Booking is already canceled.");
        }
        String oldStatus = booking.getStatus();
        booking.setStatus("CANCELED");
        booking.setNewSlot(null);
        booking.setNewVehicle(null);
        Booking savedBooking = bookingCustomerRepository.save(booking);
        notificationEventService.createBookingStatusNotification(
            booking.getCustomer(),
            booking.getBookingId().toString(),
            oldStatus,
            "CANCELED"
        );
        return mapToBookingResponse(savedBooking);
    }

    private BookingCustomerResponse mapToBookingResponse(Booking booking) {
        // Convert items to response
        java.util.List<org.example.operatormanagementsystem.customer_thai.dto.response.ItemsResponse> itemsResponses = null;
        if (booking.getItems() != null && !booking.getItems().isEmpty()) {
            itemsResponses = booking.getItems().stream()
                    .map(item -> org.example.operatormanagementsystem.customer_thai.dto.response.ItemsResponse.builder()
                            .itemId(item.getItemId())
                            .name(item.getName())
                            .quantity(item.getQuantity())
                            .weight(item.getWeight())
                            .volume(item.getVolume())
                            .modular(item.getModular())
                            .bulky(item.getBulky())
                            .room(item.getRoom())
                            .bookingId(booking.getBookingId())
                            .build())
                    .collect(Collectors.toList());
        }
        
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
                .paymentStatus(booking.getPaymentStatus())
                .promotionId(booking.getPromotion() != null ? booking.getPromotion().getId() : null)
                .promotionName(booking.getPromotion() != null ? booking.getPromotion().getName() : null)
                .promotionDescription(booking.getPromotion() != null ? booking.getPromotion().getDescription() : null)
                .homeType(booking.getHomeType())
                .items(itemsResponses)
                .slotIndex(booking.getSlotIndex())
                .vehicleQuantity(booking.getVehicleQuantity())
                .newSlot(booking.getNewSlot())
                .newVehicle(booking.getNewVehicle())
                .build();
    }

    @Override
    public java.util.List<org.example.operatormanagementsystem.customer_thai.dto.response.ItemsResponse> getBookingItems(Integer bookingId) {
        Users currentUser = customerInfoService.getCurrentCustomerUser();
        if (currentUser.getCustomer() == null) {
            throw new RuntimeException("Could not find customer profile for the current user.");
        }
        Integer customerId = currentUser.getCustomer().getCustomerId();

        // Kiểm tra booking thuộc về customer hiện tại
        Booking booking = bookingCustomerRepository.findByBookingIdAndCustomer_CustomerId(bookingId, customerId)
                .orElseThrow(() -> new RuntimeException("Booking not found or you do not have permission to view it."));

        java.util.List<org.example.operatormanagementsystem.entity.Items> items = itemsRepository.findByBookingBookingId(bookingId);
        return items.stream()
                .map(item -> org.example.operatormanagementsystem.customer_thai.dto.response.ItemsResponse.builder()
                        .itemId(item.getItemId())
                        .name(item.getName())
                        .quantity(item.getQuantity())
                        .weight(item.getWeight())
                        .volume(item.getVolume())
                        .modular(item.getModular())
                        .bulky(item.getBulky())
                        .room(item.getRoom())
                        .bookingId(bookingId)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public java.util.List<org.example.operatormanagementsystem.customer_thai.dto.response.ItemsResponse> addItemsToBooking(Integer bookingId, java.util.List<org.example.operatormanagementsystem.customer_thai.dto.request.ItemsRequest> itemsRequest) {
        Users currentUser = customerInfoService.getCurrentCustomerUser();
        if (currentUser.getCustomer() == null) {
            throw new RuntimeException("Could not find customer profile for the current user.");
        }
        Integer customerId = currentUser.getCustomer().getCustomerId();

        // Kiểm tra booking thuộc về customer hiện tại
        Booking booking = bookingCustomerRepository.findByBookingIdAndCustomer_CustomerId(bookingId, customerId)
                .orElseThrow(() -> new RuntimeException("Booking not found or you do not have permission to modify it."));

        java.util.List<org.example.operatormanagementsystem.entity.Items> items = itemsRequest.stream()
                .map(itemRequest -> org.example.operatormanagementsystem.entity.Items.builder()
                        .name(itemRequest.getName())
                        .quantity(itemRequest.getQuantity())
                        .weight(itemRequest.getWeight())
                        .volume(itemRequest.getVolume())
                        .modular(itemRequest.getModular())
                        .bulky(itemRequest.getBulky())
                        .room(itemRequest.getRoom())
                        .booking(booking)
                        .build())
                .collect(Collectors.toList());

        java.util.List<org.example.operatormanagementsystem.entity.Items> savedItems = itemsRepository.saveAll(items);
        
        return savedItems.stream()
                .map(item -> org.example.operatormanagementsystem.customer_thai.dto.response.ItemsResponse.builder()
                        .itemId(item.getItemId())
                        .name(item.getName())
                        .quantity(item.getQuantity())
                        .weight(item.getWeight())
                        .volume(item.getVolume())
                        .modular(item.getModular())
                        .bulky(item.getBulky())
                        .room(item.getRoom())
                        .bookingId(bookingId)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public org.example.operatormanagementsystem.customer_thai.dto.response.ItemsResponse updateBookingItem(Integer bookingId, Integer itemId, org.example.operatormanagementsystem.customer_thai.dto.request.ItemsRequest itemRequest) {
        Users currentUser = customerInfoService.getCurrentCustomerUser();
        if (currentUser.getCustomer() == null) {
            throw new RuntimeException("Could not find customer profile for the current user.");
        }
        Integer customerId = currentUser.getCustomer().getCustomerId();

        // Kiểm tra booking thuộc về customer hiện tại
        Booking booking = bookingCustomerRepository.findByBookingIdAndCustomer_CustomerId(bookingId, customerId)
                .orElseThrow(() -> new RuntimeException("Booking not found or you do not have permission to modify it."));

        // Tìm item cần cập nhật
        org.example.operatormanagementsystem.entity.Items item = itemsRepository.findByItemIdAndBookingBookingId(itemId, bookingId)
                .orElseThrow(() -> new RuntimeException("Item not found or does not belong to this booking."));

        // Cập nhật thông tin item
        item.setName(itemRequest.getName());
        item.setQuantity(itemRequest.getQuantity());
        item.setWeight(itemRequest.getWeight());
        item.setVolume(itemRequest.getVolume());
        item.setModular(itemRequest.getModular());
        item.setBulky(itemRequest.getBulky());
        item.setRoom(itemRequest.getRoom());

        org.example.operatormanagementsystem.entity.Items savedItem = itemsRepository.save(item);
        
        return org.example.operatormanagementsystem.customer_thai.dto.response.ItemsResponse.builder()
                .itemId(savedItem.getItemId())
                .name(savedItem.getName())
                .quantity(savedItem.getQuantity())
                .weight(savedItem.getWeight())
                .volume(savedItem.getVolume())
                .modular(savedItem.getModular())
                .bulky(savedItem.getBulky())
                .room(savedItem.getRoom())
                .bookingId(bookingId)
                .build();
    }

    @Override
    @Transactional
    public java.util.List<org.example.operatormanagementsystem.customer_thai.dto.response.ItemsResponse> updateBookingItems(Integer bookingId, java.util.List<org.example.operatormanagementsystem.customer_thai.dto.request.ItemsRequest> itemsRequest) {
        Users currentUser = customerInfoService.getCurrentCustomerUser();
        if (currentUser.getCustomer() == null) {
            throw new RuntimeException("Could not find customer profile for the current user.");
        }
        Integer customerId = currentUser.getCustomer().getCustomerId();

        // Kiểm tra booking thuộc về customer hiện tại
        Booking booking = bookingCustomerRepository.findByBookingIdAndCustomer_CustomerId(bookingId, customerId)
                .orElseThrow(() -> new RuntimeException("Booking not found or you do not have permission to modify it."));

        // Xóa items cũ
        java.util.List<org.example.operatormanagementsystem.entity.Items> existingItems = itemsRepository.findByBookingBookingId(bookingId);
        itemsRepository.deleteAll(existingItems);

        // Thêm items mới
        java.util.List<org.example.operatormanagementsystem.entity.Items> newItems = itemsRequest.stream()
                .map(itemRequest -> org.example.operatormanagementsystem.entity.Items.builder()
                        .name(itemRequest.getName())
                        .quantity(itemRequest.getQuantity())
                        .weight(itemRequest.getWeight())
                        .volume(itemRequest.getVolume())
                        .modular(itemRequest.getModular())
                        .bulky(itemRequest.getBulky())
                        .room(itemRequest.getRoom())
                        .booking(booking)
                        .build())
                .collect(Collectors.toList());

        java.util.List<org.example.operatormanagementsystem.entity.Items> savedItems = itemsRepository.saveAll(newItems);
        
        return savedItems.stream()
                .map(item -> org.example.operatormanagementsystem.customer_thai.dto.response.ItemsResponse.builder()
                        .itemId(item.getItemId())
                        .name(item.getName())
                        .quantity(item.getQuantity())
                        .weight(item.getWeight())
                        .volume(item.getVolume())
                        .modular(item.getModular())
                        .bulky(item.getBulky())
                        .room(item.getRoom())
                        .bookingId(bookingId)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteBookingItem(Integer bookingId, Integer itemId) {
        Users currentUser = customerInfoService.getCurrentCustomerUser();
        if (currentUser.getCustomer() == null) {
            throw new RuntimeException("Could not find customer profile for the current user.");
        }
        Integer customerId = currentUser.getCustomer().getCustomerId();

        // Kiểm tra booking thuộc về customer hiện tại
        Booking booking = bookingCustomerRepository.findByBookingIdAndCustomer_CustomerId(bookingId, customerId)
                .orElseThrow(() -> new RuntimeException("Booking not found or you do not have permission to modify it."));

        // Tìm và xóa item cụ thể
        org.example.operatormanagementsystem.entity.Items item = itemsRepository.findByItemIdAndBookingBookingId(itemId, bookingId)
                .orElseThrow(() -> new RuntimeException("Item not found or does not belong to this booking."));
        
        itemsRepository.delete(item);
    }

    @Override
    public SlotStatusResponse getSlotStatusByStorageId(Integer storageId) {
        StorageUnit storage = storageUnitRepository.findById(storageId)
                .orElseThrow(() -> new RuntimeException("Storage not found"));
        int totalSlots = storage.getSlotCount();
        List<Booking> bookings = bookingCustomerRepository.findByStorageUnit_StorageId(storageId);
        java.util.Map<Integer, Booking> slotBookingMap = new java.util.HashMap<>();
        for (Booking b : bookings) {
            if (b.getSlotIndex() != null) {
                slotBookingMap.put(b.getSlotIndex(), b);
            }
        }
        java.util.List<SlotStatusResponse.SlotInfo> slots = new java.util.ArrayList<>();
        for (int i = 1; i <= totalSlots; i++) { // slotIndex từ 1 đến n
            Booking b = slotBookingMap.get(i);
            if (b != null) {
                // Nếu slotIndex != newSlot và newSlot == null thì trả về slotIndex=null, booked=false, bookingId=null, customerName=null
                if ((b.getNewSlot() == null && !iEquals(b.getSlotIndex(), b.getNewSlot()))) {
                    slots.add(SlotStatusResponse.SlotInfo.builder()
                            .slotIndex(null)
                            .booked(false)
                            .bookingId(null)
                            .customerName(null)
                            .build());
                } else if (b.getNewSlot() != null && iEquals(b.getSlotIndex(), b.getNewSlot())) {
                    // Nếu slotIndex == newSlot và newSlot != null thì trả về dữ liệu booking như cũ
                    slots.add(SlotStatusResponse.SlotInfo.builder()
                            .slotIndex(i)
                            .booked(true)
                            .bookingId(b.getBookingId())
                            .customerName(b.getCustomer().getUsers().getFullName())
                            .build());
                } else {
                    // Trường hợp còn lại (slotIndex == newSlot == null hoặc khác), trả về slotIndex=null
                    slots.add(SlotStatusResponse.SlotInfo.builder()
                            .slotIndex(null)
                            .booked(false)
                            .bookingId(null)
                            .customerName(null)
                            .build());
                }
            } else {
                slots.add(SlotStatusResponse.SlotInfo.builder()
                        .slotIndex(null)
                        .booked(false)
                        .bookingId(null)
                        .customerName(null)
                        .build());
            }
        }
        return SlotStatusResponse.builder()
                .storageId(storage.getStorageId())
                .storageName(storage.getName())
                .totalSlots(totalSlots)
                .slots(slots)
                .build();
    }

    // Helper method so sánh Integer (có thể null)
    private boolean iEquals(Integer a, Integer b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    @Override
    public boolean checkVehicleAvailability(Integer transportUnitId, Integer vehicleQuantity) {
        TransportUnit transportUnit = transportUnitRepository.findById(transportUnitId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn vị vận chuyển"));
        int totalVehicles = transportUnit.getNumberOfVehicles() != null ? transportUnit.getNumberOfVehicles() : 0;
        // Lấy tất cả booking của transportUnit này
        java.util.List<Booking> bookings = bookingCustomerRepository.findByTransportUnit_TransportId(transportUnitId);
        int bookedVehicles = 0;
        for (Booking booking : bookings) {
            // Chỉ cộng số xe của các booking mà newVehicle != null và newVehicle == vehicleQuantity
            if (booking.getNewVehicle() != null && booking.getNewVehicle().equals(booking.getVehicleQuantity())) {
                bookedVehicles += booking.getVehicleQuantity() != null ? booking.getVehicleQuantity() : 0;
            }
            // Nếu newVehicle == null thì bỏ qua, không cộng vào
        }
        return (bookedVehicles + vehicleQuantity) <= totalVehicles;
    }
} 