package org.example.operatormanagementsystem.customer_thai.service;

import org.example.operatormanagementsystem.customer_thai.dto.request.CreateBookingRequest;
import org.example.operatormanagementsystem.customer_thai.dto.response.BookingCustomerResponse;
import org.example.operatormanagementsystem.customer_thai.dto.response.SlotStatusResponse;

import java.util.List;

public interface BookingCustomerService {
    /**
     * Creates a new booking for the currently authenticated customer.
     * @param request The request containing booking details.
     * @return The created booking details.
     */
    BookingCustomerResponse createBooking(CreateBookingRequest request);

    /**
     * Retrieves all bookings for the currently authenticated customer.
     * @return A list of booking details.
     */
    List<BookingCustomerResponse> getAllMyBookings();

    /**
     * Retrieves a specific booking by its ID for the currently authenticated customer.
     * @param bookingId The ID of the booking to retrieve.
     * @return The booking details.
     */
    BookingCustomerResponse getBookingById(Integer bookingId);

    /**
     * Deletes a specific booking by its ID for the currently authenticated customer.
     * @param bookingId The ID of the booking to delete.
     */
    void deleteBooking(Integer bookingId);

    /**
     * Gets all items for a specific booking.
     * @param bookingId The ID of the booking.
     * @return List of items for the booking.
     */
    java.util.List<org.example.operatormanagementsystem.customer_thai.dto.response.ItemsResponse> getBookingItems(Integer bookingId);

    /**
     * Adds items to a specific booking.
     * @param bookingId The ID of the booking.
     * @param itemsRequest List of items to add.
     * @return List of added items.
     */
    java.util.List<org.example.operatormanagementsystem.customer_thai.dto.response.ItemsResponse> addItemsToBooking(Integer bookingId, java.util.List<org.example.operatormanagementsystem.customer_thai.dto.request.ItemsRequest> itemsRequest);

    /**
     * Updates a specific item in a booking.
     * @param bookingId The ID of the booking.
     * @param itemId The ID of the item to update.
     * @param itemRequest The updated item data.
     * @return The updated item.
     */
    org.example.operatormanagementsystem.customer_thai.dto.response.ItemsResponse updateBookingItem(Integer bookingId, Integer itemId, org.example.operatormanagementsystem.customer_thai.dto.request.ItemsRequest itemRequest);

    /**
     * Updates items for a specific booking.
     * @param bookingId The ID of the booking.
     * @param itemsRequest List of items to update.
     * @return List of updated items.
     */
    java.util.List<org.example.operatormanagementsystem.customer_thai.dto.response.ItemsResponse> updateBookingItems(Integer bookingId, java.util.List<org.example.operatormanagementsystem.customer_thai.dto.request.ItemsRequest> itemsRequest);

    /**
     * Deletes a specific item from a booking.
     * @param bookingId The ID of the booking.
     * @param itemId The ID of the item to delete.
     */
    void deleteBookingItem(Integer bookingId, Integer itemId);

    SlotStatusResponse getSlotStatusByStorageId(Integer storageId);

    /**
     * Kiểm tra số lượng xe còn lại của transport unit
     */
    boolean checkVehicleAvailability(Integer transportUnitId, Integer vehicleQuantity);
} 