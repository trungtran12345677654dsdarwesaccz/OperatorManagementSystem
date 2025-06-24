package org.example.operatormanagementsystem.customer_thai.service;

import org.example.operatormanagementsystem.customer_thai.dto.request.CreateBookingRequest;
import org.example.operatormanagementsystem.customer_thai.dto.response.BookingCustomerResponse;

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
} 