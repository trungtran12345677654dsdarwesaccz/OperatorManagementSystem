package org.example.operatormanagementsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.operatormanagementsystem.dto.request.TimeOffApprovalRequest;
import org.example.operatormanagementsystem.dto.request.TimeOffRequestDto;
import org.example.operatormanagementsystem.dto.response.TimeOffStatusResponse;
import org.example.operatormanagementsystem.entity.*;
import org.example.operatormanagementsystem.enumeration.TimeOffStatus;
import org.example.operatormanagementsystem.managercustomer.dto.response.UserSearchResponse;
import org.example.operatormanagementsystem.managercustomer.service.UserService;
import org.example.operatormanagementsystem.managestaff_yen.repository.OperatorStaffRepository;
import org.example.operatormanagementsystem.repository.ShiftAssignmentRepository;
import org.example.operatormanagementsystem.repository.TimeOffRequestRepository;
import org.example.operatormanagementsystem.schedulemanagement.service.ScheduleNotificationService;
import org.example.operatormanagementsystem.service.TimeOffService;
import org.example.operatormanagementsystem.transportunit.repository.ManagerRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TimeOffServiceImpl implements TimeOffService {

    private final TimeOffRequestRepository timeOffRequestRepository;
    private final UserService userService;
    private final ManagerRepository managerRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final ScheduleNotificationService scheduleNotificationService;

    @Override
    public TimeOffStatusResponse submitTimeOffRequest(TimeOffRequestDto request) {
        log.debug("Submitting time-off request for operator {}", request.getOperatorId());

        validateTimeOffRequest(request);

        Users operator = userService.findById(request.getOperatorId())
                .orElseThrow(() -> new RuntimeException("Operator not found with ID: " + request.getOperatorId()));

        // Check for conflicts
        boolean hasConflicts = hasConflictWithAssignments(
                request.getOperatorId(), request.getStartDate(), request.getEndDate());

        String conflictDetails = hasConflicts ?
                getConflictDetails(request.getOperatorId(), request.getStartDate(), request.getEndDate()) : null;

        TimeOffRequest timeOffRequest = TimeOffRequest.builder()
                .operator(operator)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .status(TimeOffStatus.PENDING)
                .build();

        TimeOffRequest savedRequest = timeOffRequestRepository.save(timeOffRequest);
        log.info("Created time-off request with ID: {} for operator: {}",
                savedRequest.getRequestId(), request.getOperatorId());


        TimeOffStatusResponse response = mapToTimeOffStatusResponse(savedRequest);
        response.setHasConflicts(hasConflicts);
        response.setConflictDetails(conflictDetails);

        return response;
    }

    @Override
    public TimeOffStatusResponse approveTimeOffRequest(TimeOffApprovalRequest request) {
        try {
            TimeOffRequest timeOffRequest = timeOffRequestRepository.findById(request.getRequestId())
                    .orElseThrow(() -> new RuntimeException("Time-off request not found with ID: " + request.getRequestId()));

            if (timeOffRequest.getStatus() != TimeOffStatus.PENDING) {
                throw new RuntimeException("Only pending requests can be approved");
            }
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            Users user = userService.findUsersResponseByEmail(email);

            timeOffRequest.setStatus(TimeOffStatus.APPROVED);
            timeOffRequest.setManagerComments(request.getManagerComments());
            timeOffRequest.setReviewedBy(user);
            timeOffRequest.setReviewedDate(LocalDateTime.now());

            TimeOffRequest savedRequest = timeOffRequestRepository.save(timeOffRequest);

            return mapToTimeOffStatusResponse(savedRequest);
        } catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("Time-off request cannot be approved" + e.getMessage());
        }
    }


    @Override
    public TimeOffStatusResponse rejectTimeOffRequest(TimeOffApprovalRequest request) {


        TimeOffRequest timeOffRequest = timeOffRequestRepository.findById(request.getRequestId())
                .orElseThrow(() -> new RuntimeException("Time-off request not found with ID: " + request.getRequestId()));

        if (timeOffRequest.getStatus() != TimeOffStatus.PENDING) {
            throw new RuntimeException("Only pending requests can be rejected");
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Users user = userService.findUsersResponseByEmail(email);

        timeOffRequest.setStatus(TimeOffStatus.REJECTED);
        timeOffRequest.setManagerComments(request.getManagerComments());
        timeOffRequest.setReviewedBy(user);
        timeOffRequest.setReviewedDate(LocalDateTime.now());

        TimeOffRequest savedRequest = timeOffRequestRepository.save(timeOffRequest);
        return mapToTimeOffStatusResponse(savedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public TimeOffStatusResponse getTimeOffRequestById(Integer requestId) {
        log.debug("Getting time-off request by ID: {}", requestId);

        TimeOffRequest timeOffRequest = timeOffRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Time-off request not found with ID: " + requestId));

        return mapToTimeOffStatusResponse(timeOffRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeOffStatusResponse> getOperatorTimeOffRequests(Integer operatorId) {
        log.debug("Getting time-off requests for operator: {}", operatorId);

        List<TimeOffRequest> requests = timeOffRequestRepository.findByOperatorId(operatorId);
        return requests.stream()
                .map(this::mapToTimeOffStatusResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeOffStatusResponse> getTimeOffRequestsByStatus(TimeOffStatus status) {
        log.debug("Getting time-off requests by status: {}", status);

        List<TimeOffRequest> requests = timeOffRequestRepository.findByStatusOrderByRequestDateDesc(status);
        return requests.stream()
                .map(this::mapToTimeOffStatusResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeOffStatusResponse> getPendingTimeOffRequests() {
        return getTimeOffRequestsByStatus(TimeOffStatus.PENDING);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeOffStatusResponse> getTimeOffRequestsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Getting time-off requests from {} to {}", startDate, endDate);

        List<TimeOffRequest> requests = timeOffRequestRepository.findAll().stream()
                .filter(request -> !request.getRequestDate().isBefore(startDate) &&
                        !request.getRequestDate().isAfter(endDate))
                .collect(Collectors.toList());

        return requests.stream()
                .map(this::mapToTimeOffStatusResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeOffStatusResponse> getOperatorTimeOffRequestsByDateRange(
            Integer operatorId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Getting time-off requests for operator {} from {} to {}", operatorId, startDate, endDate);

        List<TimeOffRequest> requests = timeOffRequestRepository
                .findByOperatorAndDateRange(operatorId, startDate, endDate);

        return requests.stream()
                .map(this::mapToTimeOffStatusResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasConflictWithAssignments(Integer operatorId, LocalDate startDate, LocalDate endDate) {
        log.debug("Checking conflicts for operator {} from {} to {}", operatorId, startDate, endDate);

        // Check for existing shift assignments
        List<ShiftAssignment> assignments = shiftAssignmentRepository
                .findByOperatorIdAndAssignmentDateBetween(operatorId, startDate, endDate);

        if (!assignments.isEmpty()) {
            log.debug("Found {} conflicting shift assignments", assignments.size());
            return true;
        }

        // Check for overlapping approved time-off requests
        List<TimeOffRequest> overlappingRequests = timeOffRequestRepository
                .findOverlappingApprovedRequests(operatorId, startDate, endDate);

        if (!overlappingRequests.isEmpty()) {
            log.debug("Found {} overlapping time-off requests", overlappingRequests.size());
            return true;
        }

        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public String getConflictDetails(Integer operatorId, LocalDate startDate, LocalDate endDate) {
        StringBuilder details = new StringBuilder();

        // Check shift assignments
        List<ShiftAssignment> assignments = shiftAssignmentRepository
                .findByOperatorIdAndAssignmentDateBetween(operatorId, startDate, endDate);

        if (!assignments.isEmpty()) {
            details.append("Conflicting shift assignments: ");
            details.append(assignments.stream()
                    .map(assignment -> assignment.getAssignmentDate() + " (" +
                            assignment.getWorkShift().getShiftName() + ")")
                    .collect(Collectors.joining(", ")));
        }

        // Check overlapping time-off
        List<TimeOffRequest> overlappingRequests = timeOffRequestRepository
                .findOverlappingApprovedRequests(operatorId, startDate, endDate);

        if (!overlappingRequests.isEmpty()) {
            if (details.length() > 0) {
                details.append("; ");
            }
            details.append("Overlapping time-off requests: ");
            details.append(overlappingRequests.stream()
                    .map(request -> request.getStartDate() + " to " + request.getEndDate())
                    .collect(Collectors.joining(", ")));
        }

        return details.toString();
    }

    @Override
    public void validateTimeOffRequest(TimeOffRequestDto request) {
        if (request.getOperatorId() == null) {
            throw new RuntimeException("Operator ID is required");
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new RuntimeException("Start date must be before or equal to end date");
        }

        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot request time off for past dates");
        }

        // Check if the date range is reasonable (e.g., not more than 365 days)
        long daysBetween = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        if (daysBetween > 365) {
            throw new RuntimeException("Time-off request cannot exceed 365 days");
        }

        if (request.getReason() == null || request.getReason().trim().length() < 5) {
            throw new RuntimeException("Reason must be at least 5 characters long");
        }
    }

    @Override
    public void cancelTimeOffRequest(Integer requestId, Integer operatorId) {
        log.debug("Cancelling time-off request ID: {} by operator: {}", requestId, operatorId);

        TimeOffRequest timeOffRequest = timeOffRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Time-off request not found with ID: " + requestId));

        if (!timeOffRequest.getOperator().getId().equals(operatorId)) {
            throw new RuntimeException("Operator can only cancel their own requests");
        }

        if (timeOffRequest.getStatus() != TimeOffStatus.PENDING) {
            throw new RuntimeException("Only pending requests can be cancelled");
        }

        timeOffRequestRepository.delete(timeOffRequest);
        log.info("Cancelled time-off request ID: {}", requestId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeOffStatusResponse> getApprovedTimeOffForDate(Integer operatorId, LocalDate date) {
        List<TimeOffRequest> approvedRequests = timeOffRequestRepository
                .findApprovedTimeOffForDate(operatorId, date);

        return approvedRequests.stream()
                .map(this::mapToTimeOffStatusResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countPendingRequests() {
        return timeOffRequestRepository.countPendingRequests();
    }

    private TimeOffStatusResponse mapToTimeOffStatusResponse(TimeOffRequest request) {
        int totalDays = (int) ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;

        return TimeOffStatusResponse.builder()
                .requestId(request.getRequestId())
                .operatorId(request.getOperator().getId())
                .operatorName(request.getOperator().getFullName())
                .operatorEmail(request.getOperator().getEmail())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .status(request.getStatus())
                .managerComments(request.getManagerComments())
                .requestDate(request.getRequestDate())
                .reviewedDate(request.getReviewedDate())
                .reviewedByName(request.getReviewedBy() != null && request.getReviewedBy() != null ?
                        request.getReviewedBy().getFullName() : null)
                .reviewedById(request.getReviewedBy() != null ? request.getReviewedBy().getId() : null)
                .totalDays(totalDays)
                .hasConflicts(false) // Will be set separately when needed
                .conflictDetails(null) // Will be set separately when needed
                .build();
    }
}