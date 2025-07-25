package org.example.operatormanagementsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.operatormanagementsystem.dto.request.CreateShiftRequest;
import org.example.operatormanagementsystem.dto.request.ShiftAssignmentRequest;
import org.example.operatormanagementsystem.dto.response.ShiftDetailsResponse;
import org.example.operatormanagementsystem.entity.*;
import org.example.operatormanagementsystem.enumeration.ShiftAssignmentStatus;
import org.example.operatormanagementsystem.managercustomer.dto.response.UserSearchResponse;
import org.example.operatormanagementsystem.managercustomer.service.UserService;
import org.example.operatormanagementsystem.managestaff_yen.repository.OperatorStaffRepository;
import org.example.operatormanagementsystem.repository.ShiftAssignmentRepository;
import org.example.operatormanagementsystem.repository.TimeOffRequestRepository;
import org.example.operatormanagementsystem.repository.WorkShiftRepository;
import org.example.operatormanagementsystem.schedulemanagement.service.ScheduleNotificationService;
import org.example.operatormanagementsystem.service.ShiftService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ShiftServiceImpl implements ShiftService {

    private final WorkShiftRepository workShiftRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final TimeOffRequestRepository timeOffRequestRepository;
    private final ScheduleNotificationService scheduleNotificationService;
    private final UserService userService;

    @Override
    public ShiftDetailsResponse createShift(CreateShiftRequest request) {
        log.debug("Creating new shift: {}", request.getShiftName());

        validateShiftTimes(request);

        WorkShift workShift = WorkShift.builder()
                .shiftName(request.getShiftName())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .description(request.getDescription())
                .isActive(request.getIsActive())
                .build();

        WorkShift savedShift = workShiftRepository.save(workShift);
        log.info("Created shift with ID: {}", savedShift.getShiftId());

        return mapToShiftDetailsResponse(savedShift);
    }

    @Override
    public ShiftDetailsResponse updateShift(Integer shiftId, CreateShiftRequest request) {
        log.debug("Updating shift ID: {}", shiftId);

        WorkShift existingShift = workShiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found with ID: " + shiftId));

        validateShiftTimes(request);

        existingShift.setShiftName(request.getShiftName());
        existingShift.setStartTime(request.getStartTime());
        existingShift.setEndTime(request.getEndTime());
        existingShift.setDescription(request.getDescription());
        log.info("Updating isActive from {} to {}", existingShift.getIsActive(), request.getIsActive());
        existingShift.setIsActive(request.getIsActive());

        WorkShift updatedShift = workShiftRepository.save(existingShift);
        log.info("Updated shift ID: {} with isActive: {}", shiftId, updatedShift.getIsActive());

        return mapToShiftDetailsResponse(updatedShift);
    }

    @Override
    @Transactional(readOnly = true)
    public ShiftDetailsResponse getShiftById(Integer shiftId) {
        log.debug("Getting shift by ID: {}", shiftId);

        WorkShift workShift = workShiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found with ID: " + shiftId));

        return mapToShiftDetailsResponse(workShift);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftDetailsResponse> getAllActiveShifts() {
        log.debug("Getting all active shifts");

        List<WorkShift> activeShifts = workShiftRepository.findAllActiveOrderByStartTime();
        return activeShifts.stream()
                .map(this::mapToShiftDetailsResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftDetailsResponse> getAllShifts() {
        log.debug("Getting all shifts");

        List<WorkShift> allShifts = workShiftRepository.findAll();
        return allShifts.stream()
                .map(this::mapToShiftDetailsResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteShift(Integer shiftId) {
        log.debug("Deleting shift ID: {}", shiftId);

        if (!workShiftRepository.existsById(shiftId)) {
            throw new RuntimeException("Shift not found with ID: " + shiftId);
        }
        workShiftRepository.deleteById(shiftId);
        log.info("Hard deleted shift ID: {}", shiftId);
    }

    @Override
    public void assignOperatorsToShift(ShiftAssignmentRequest request) {
        log.debug("Assigning operators to shift ID: {} for date: {}",
                request.getShiftId(), request.getAssignmentDate());

        WorkShift workShift = workShiftRepository.findById(request.getShiftId())
                .orElseThrow(() -> new RuntimeException("Shift not found with ID: " + request.getShiftId()));

        List<ShiftAssignment> assignments = new ArrayList<>();

        for (Integer operatorId : request.getOperatorIds()) {
            // Load user và check role
            Users user = userService.findById(operatorId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + operatorId));
            if (user.getRole() == null || !user.getRole().name().equals("STAFF")) {
                log.warn("User {} is not STAFF, skipping", operatorId);
                continue;
            }

            // Check for conflicts
            if (hasTimeConflict(request.getShiftId(), operatorId, request.getAssignmentDate())) {
                throw new RuntimeException("Time conflict detected for user ID: " + operatorId);
            }
            // Check if assignment already exists
            if (shiftAssignmentRepository.existsByOperatorIdAndWorkShiftShiftIdAndAssignmentDate(
                    operatorId, request.getShiftId(), request.getAssignmentDate())) {
                log.warn("Assignment already exists for user {} on shift {} for date {}",
                        operatorId, request.getShiftId(), request.getAssignmentDate());
                continue;
            }
            ShiftAssignment assignment = ShiftAssignment.builder()
                    .operator(user)
                    .workShift(workShift)
                    .assignmentDate(request.getAssignmentDate())
                    .status(ShiftAssignmentStatus.ASSIGNED)
                    .build();
            assignments.add(assignment);
        }
        List<ShiftAssignment> savedAssignments = shiftAssignmentRepository.saveAll(assignments);
        log.info("Assigned {} users to shift ID: {} for date: {}",
                assignments.size(), request.getShiftId(), request.getAssignmentDate());
        // Send notifications if possible
        for (ShiftAssignment assignment : savedAssignments) {
            if (assignment.getOperator() != null) {
                try {
                    scheduleNotificationService.notifyShiftAssignment(assignment.getOperator(), assignment);
                } catch (Exception e) {
                    log.error("Failed to send shift assignment notification to operator {}: {}",
                            assignment.getOperator().getId(), e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void removeOperatorAssignment(Integer shiftId, Integer operatorId, LocalDate assignmentDate) {
        log.debug("Removing operator {} from shift {} for date {}", operatorId, shiftId, assignmentDate);

        List<ShiftAssignment> assignments = shiftAssignmentRepository
                .findByOperatorIdAndAssignmentDate(operatorId, assignmentDate)
                .stream()
                .filter(assignment -> assignment.getWorkShift().getShiftId().equals(shiftId))
                .collect(Collectors.toList());

        if (assignments.isEmpty()) {
            throw new RuntimeException("Assignment not found for operator " + operatorId +
                    " on shift " + shiftId + " for date " + assignmentDate);
        }

        // Send cancellation notifications before deleting
        for (ShiftAssignment assignment : assignments) {
            try {
                scheduleNotificationService.notifyShiftCancellation(assignment.getOperator(), assignment);
            } catch (Exception e) {
                log.error("Failed to send shift cancellation notification to operator {}: {}",
                        assignment.getOperator().getId(), e.getMessage(), e);
            }
        }

        shiftAssignmentRepository.deleteAll(assignments);
        log.info("Removed operator {} from shift {} for date {}", operatorId, shiftId, assignmentDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftDetailsResponse> getOperatorShiftAssignments(Integer operatorId, LocalDate startDate, LocalDate endDate) {
        log.debug("Getting shift assignments for operator {} from {} to {}", operatorId, startDate, endDate);

        List<ShiftAssignment> assignments = shiftAssignmentRepository
                .findOperatorSchedule(operatorId, startDate, endDate);

        return assignments.stream()
                .map(assignment -> mapToShiftDetailsResponse(assignment.getWorkShift()))
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasTimeConflict(Integer shiftId, Integer operatorId, LocalDate assignmentDate) {
        log.debug("Checking time conflict for operator {} on shift {} for date {}",
                operatorId, shiftId, assignmentDate);

        // Check for approved time off
        List<TimeOffRequest> timeOffRequests = timeOffRequestRepository
                .findApprovedTimeOffForDate(operatorId, assignmentDate);

        if (!timeOffRequests.isEmpty()) {
            log.debug("Operator {} has approved time off on {}", operatorId, assignmentDate);
            return true;
        }

        // Check for overlapping shift assignments
        List<ShiftAssignment> existingAssignments = shiftAssignmentRepository
                .findByOperatorIdAndAssignmentDate(operatorId, assignmentDate);

        if (!existingAssignments.isEmpty()) {
            WorkShift newShift = workShiftRepository.findById(shiftId)
                    .orElseThrow(() -> new RuntimeException("Shift not found with ID: " + shiftId));

            for (ShiftAssignment assignment : existingAssignments) {
                WorkShift existingShift = assignment.getWorkShift();

                // Check for time overlap
                if (shiftsOverlap(newShift, existingShift)) {
                    log.debug("Time conflict detected between shifts {} and {}",
                            newShift.getShiftName(), existingShift.getShiftName());
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void validateShiftTimes(CreateShiftRequest request) {
        if (request.getStartTime().isAfter(request.getEndTime()) ||
                request.getStartTime().equals(request.getEndTime())) {
            throw new RuntimeException("Start time must be before end time");
        }

        // Check for overlapping shifts (optional business rule)
        List<WorkShift> overlappingShifts = workShiftRepository
                .findOverlappingShifts(request.getStartTime(), request.getEndTime());

        if (!overlappingShifts.isEmpty()) {
            log.warn("Creating shift with overlapping times. Existing overlapping shifts: {}",
                    overlappingShifts.stream()
                            .map(WorkShift::getShiftName)
                            .collect(Collectors.joining(", ")));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSearchResponse> getAvailableOperators(Integer shiftId, LocalDate assignmentDate) {
        List<Users> allStaff = userService.findAll();
        return allStaff.stream()
                .filter(user -> user.getRole() != null && user.getRole().name().equals("STAFF"))
                .filter(user -> !hasTimeConflict(shiftId, user.getId(), assignmentDate))
                .map(this::mapUserToUserSearchResponse)
                .collect(Collectors.toList());
    }

    private boolean shiftsOverlap(WorkShift shift1, WorkShift shift2) {
        return shift1.getStartTime().isBefore(shift2.getEndTime()) &&
                shift2.getStartTime().isBefore(shift1.getEndTime());
    }

    private ShiftDetailsResponse mapToShiftDetailsResponse(WorkShift workShift) {
        // Lấy tất cả các assignment của ca này
        List<ShiftAssignment> assignments = shiftAssignmentRepository.findByWorkShiftShiftId(workShift.getShiftId());
        List<ShiftDetailsResponse.OperatorAssignment> assignedOperators = assignments.stream()
                .map(assignment -> {
                    Users user = assignment.getOperator();
                    return ShiftDetailsResponse.OperatorAssignment.builder()
                            .operatorId(user != null ? user.getId() : null)
                            .operatorName(user != null ? user.getFullName() : "Không rõ tên")
                            .email(user != null ? user.getEmail() : null)
                            .assignmentStatus(assignment.getStatus() != null ? assignment.getStatus().name() : null)
                            .assignmentDate(assignment.getAssignmentDate() != null ? assignment.getAssignmentDate().toString() : null)
                            .build();
                })
                .collect(Collectors.toList());
        return ShiftDetailsResponse.builder()
                .shiftId(workShift.getShiftId())
                .shiftName(workShift.getShiftName())
                .startTime(workShift.getStartTime())
                .endTime(workShift.getEndTime())
                .description(workShift.getDescription())
                .isActive(workShift.getIsActive())
                .assignedOperators(assignedOperators)
                .build();
    }

    private UserSearchResponse mapUserToUserSearchResponse(Users user) {
        return UserSearchResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .gender(user.getGender() != null ? user.getGender().name() : null)
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .createdAt(user.getCreatedAt())
                .build();
    }
}