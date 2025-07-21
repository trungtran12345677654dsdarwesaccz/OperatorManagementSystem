package org.example.operatormanagementsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.operatormanagementsystem.dto.response.ScheduleCalendarResponse;
import org.example.operatormanagementsystem.entity.Booking;
import org.example.operatormanagementsystem.entity.ShiftAssignment;
import org.example.operatormanagementsystem.entity.TimeOffRequest;
import org.example.operatormanagementsystem.managecustomerorderbystaff.repository.BookingRepository;
import org.example.operatormanagementsystem.repository.ShiftAssignmentRepository;
import org.example.operatormanagementsystem.repository.TimeOffRequestRepository;
import org.example.operatormanagementsystem.service.ScheduleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ScheduleServiceImpl implements ScheduleService {
    
    private final BookingRepository bookingRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final TimeOffRequestRepository timeOffRequestRepository;
    
    @Override
    public ScheduleCalendarResponse getCalendarData(Integer operatorId, LocalDate date) {
        log.debug("Getting calendar data for operator {} on date {}", operatorId, date);
        
        // Get orders for the specific date
        List<ScheduleCalendarResponse.OrderSummary> orders = getOrdersForDate(operatorId, date);
        
        // Get shift assignments for the date
        List<ScheduleCalendarResponse.ShiftInfo> shifts = getShiftsForDate(operatorId, date);
        
        // Get time off status for the date
        ScheduleCalendarResponse.TimeOffStatus timeOffStatus = getTimeOffStatusForDate(operatorId, date);
        
        // Determine work status
        String workStatus = determineWorkStatus(timeOffStatus, shifts, orders);
        
        return ScheduleCalendarResponse.builder()
                .date(date)
                .orders(orders)
                .shifts(shifts)
                .timeOffStatus(timeOffStatus)
                .totalOrders(orders.size())
                .workStatus(workStatus)
                .build();
    }
    
    @Override
    public List<ScheduleCalendarResponse> getCalendarDataRange(Integer operatorId, LocalDate startDate, LocalDate endDate) {
        log.debug("Getting calendar data range for operator {} from {} to {}", operatorId, startDate, endDate);
        
        List<ScheduleCalendarResponse> result = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            result.add(getCalendarData(operatorId, currentDate));
            currentDate = currentDate.plusDays(1);
        }
        
        return result;
    }
    
    @Override
    public List<ScheduleCalendarResponse> getWeeklySchedule(Integer operatorId, LocalDate weekStartDate) {
        LocalDate weekEndDate = weekStartDate.plusDays(6);
        return getCalendarDataRange(operatorId, weekStartDate, weekEndDate);
    }
    
    @Override
    public List<ScheduleCalendarResponse> getMonthlySchedule(Integer operatorId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        return getCalendarDataRange(operatorId, startDate, endDate);
    }
    
    @Override
    public List<ScheduleCalendarResponse.OrderSummary> getAssignedOrders(Integer operatorId, LocalDate startDate, LocalDate endDate) {
        log.debug("Getting assigned orders for operator {} from {} to {}", operatorId, startDate, endDate);
        
        // Query bookings assigned to the operator within the date range
        List<Booking> bookings = bookingRepository.findAll().stream()
                .filter(booking -> booking.getOperatorStaff() != null && 
                                 booking.getOperatorStaff().getOperatorId().equals(operatorId))
                .filter(booking -> booking.getDeliveryDate() != null)
                .filter(booking -> {
                    LocalDate deliveryDate = booking.getDeliveryDate().toLocalDate();
                    return !deliveryDate.isBefore(startDate) && !deliveryDate.isAfter(endDate);
                })
                .collect(Collectors.toList());
        
        return bookings.stream()
                .map(this::mapBookingToOrderSummary)
                .collect(Collectors.toList());
    }
    
    private List<ScheduleCalendarResponse.OrderSummary> getOrdersForDate(Integer operatorId, LocalDate date) {
        return getAssignedOrders(operatorId, date, date);
    }
    
    private List<ScheduleCalendarResponse.ShiftInfo> getShiftsForDate(Integer operatorId, LocalDate date) {
        List<ShiftAssignment> assignments = shiftAssignmentRepository
                .findByOperatorIdAndAssignmentDate(operatorId, date);
        
        return assignments.stream()
                .map(this::mapShiftAssignmentToShiftInfo)
                .collect(Collectors.toList());
    }
    
    private ScheduleCalendarResponse.TimeOffStatus getTimeOffStatusForDate(Integer operatorId, LocalDate date) {
        List<TimeOffRequest> timeOffRequests = timeOffRequestRepository
                .findApprovedTimeOffForDate(operatorId, date);
        
        if (timeOffRequests.isEmpty()) {
            return ScheduleCalendarResponse.TimeOffStatus.builder()
                    .hasTimeOff(false)
                    .build();
        }
        
        TimeOffRequest timeOff = timeOffRequests.get(0);
        return ScheduleCalendarResponse.TimeOffStatus.builder()
                .hasTimeOff(true)
                .reason(timeOff.getReason())
                .status(timeOff.getStatus().name())
                .build();
    }
    
    private String determineWorkStatus(ScheduleCalendarResponse.TimeOffStatus timeOffStatus, 
                                     List<ScheduleCalendarResponse.ShiftInfo> shifts, 
                                     List<ScheduleCalendarResponse.OrderSummary> orders) {
        if (timeOffStatus.isHasTimeOff()) {
            return "TIME_OFF";
        }
        
        if (!shifts.isEmpty() || !orders.isEmpty()) {
            return "WORKING";
        }
        
        return "AVAILABLE";
    }
    
    private ScheduleCalendarResponse.OrderSummary mapBookingToOrderSummary(Booking booking) {
        return ScheduleCalendarResponse.OrderSummary.builder()
                .bookingId(booking.getBookingId())
                .customerName(booking.getCustomer() != null && booking.getCustomer().getUsers() != null ? 
                            booking.getCustomer().getUsers().getFullName() : "Unknown Customer")
                .pickupLocation(booking.getPickupLocation())
                .deliveryLocation(booking.getDeliveryLocation())
                .status(booking.getStatus())
                .deliveryTime(booking.getDeliveryDate() != null ? 
                            booking.getDeliveryDate().format(DateTimeFormatter.ofPattern("HH:mm")) : "")
                .total(booking.getTotal())
                .build();
    }
    
    private ScheduleCalendarResponse.ShiftInfo mapShiftAssignmentToShiftInfo(ShiftAssignment assignment) {
        return ScheduleCalendarResponse.ShiftInfo.builder()
                .shiftId(assignment.getWorkShift().getShiftId())
                .shiftName(assignment.getWorkShift().getShiftName())
                .startTime(assignment.getWorkShift().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                .endTime(assignment.getWorkShift().getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                .status(assignment.getStatus().name())
                .build();
    }
}