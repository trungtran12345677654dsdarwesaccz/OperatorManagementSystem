package org.example.operatormanagementsystem.managestaff_yen.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.operatormanagementsystem.entity.*;
import org.example.operatormanagementsystem.enumeration.UserStatus;
import org.example.operatormanagementsystem.managestaff_yen.dto.request.*;
import org.example.operatormanagementsystem.managestaff_yen.dto.response.*;
import org.example.operatormanagementsystem.managestaff_yen.repository.*;
import org.example.operatormanagementsystem.managestaff_yen.service.StaffManagementService;
import org.example.operatormanagementsystem.transportunit.repository.ManagerRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StaffManagementServiceImpl implements StaffManagementService {

    private final OperatorStaffRepository operatorStaffRepository;
    private final ManagerRepository managerRepository;
    private final UsersRepository usersRepository;
    private final ManagerFeedbackToStaffRepository managerFeedbackToStaffRepository;

    @Override
    @Transactional(readOnly = true)
    public StaffListResponse viewStaffInformation(Integer managerId, int page, int size, String sortBy, String sortDir) {
        validateManagerExists(managerId);
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<OperatorStaff> staffPage = operatorStaffRepository.findByManagerManagerId(managerId, pageable);

        List<OperatorStaffResponse> staffResponses = staffPage.getContent().stream()
                .map(this::convertToStaffResponse)
                .collect(Collectors.toList());

        return StaffListResponse.builder()
                .staffs(staffResponses)
                .currentPage(staffPage.getNumber())
                .totalPages(staffPage.getTotalPages())
                .totalElements(staffPage.getTotalElements())
                .pageSize(staffPage.getSize())
                .hasNext(staffPage.hasNext())
                .hasPrevious(staffPage.hasPrevious())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StaffListResponse searchStaff(Integer managerId, String searchTerm, int page, int size) {
        validateManagerExists(managerId);
        Pageable pageable = PageRequest.of(page, size);
        Page<OperatorStaff> staffPage = operatorStaffRepository.searchStaffByManagerAndTerm(managerId, searchTerm, pageable);

        List<OperatorStaffResponse> staffResponses = staffPage.getContent().stream()
                .map(this::convertToStaffResponse)
                .collect(Collectors.toList());

        return StaffListResponse.builder()
                .staffs(staffResponses)
                .currentPage(staffPage.getNumber())
                .totalPages(staffPage.getTotalPages())
                .totalElements(staffPage.getTotalElements())
                .pageSize(staffPage.getSize())
                .hasNext(staffPage.hasNext())
                .hasPrevious(staffPage.hasPrevious())
                .build();
    }

    @Override
    public OperatorStaffResponse updateStaffInformation(Integer managerId, Integer operatorId, UpdateStaffRequest request) {
        OperatorStaff staff = validateStaffBelongsToManager(managerId, operatorId);
        Users user = staff.getUsers();
        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setGender(request.getGender());
        user.setStatus(request.getStatus());
        usersRepository.save(user);
        return convertToStaffResponse(staff);
    }

    @Override
    public void blockOrDeleteStaffAccount(Integer managerId, Integer operatorId, boolean permanentDelete) {
        OperatorStaff staff = validateStaffBelongsToManager(managerId, operatorId);
        Users user = staff.getUsers();

        if (permanentDelete) {
            operatorStaffRepository.delete(staff);
            usersRepository.delete(user);
        } else {
            user.setStatus(UserStatus.BLOCKED);
            usersRepository.save(user);
        }
    }

    @Override
    public void feedbackToStaff(Integer managerId, Integer operatorId, ManagerFeedbackRequest request) {
        Manager manager = validateManagerExists(managerId);
        OperatorStaff staff = validateStaffBelongsToManager(managerId, operatorId);

        ManagerFeedbackToStaff feedback = ManagerFeedbackToStaff.builder()
                .manager(manager)
                .operatorStaff(staff)
                .content(request.getContent())
                .rating(request.getRating())
                .createdAt(LocalDateTime.now())
                .build();

        managerFeedbackToStaffRepository.save(feedback);
    }

    @Override
    @Transactional(readOnly = true)
    public StaffOverviewResponse getStaffOverview(Integer managerId) {
        validateManagerExists(managerId);
        long total = operatorStaffRepository.countByManagerManagerId(managerId);
        long active = operatorStaffRepository.findByManagerManagerIdAndUsersStatus(managerId, UserStatus.ACTIVE).size();
        long inactive = operatorStaffRepository.findByManagerManagerIdAndUsersStatus(managerId, UserStatus.INACTIVE).size();
        long blocked = operatorStaffRepository.findByManagerManagerIdAndUsersStatus(managerId, UserStatus.BLOCKED).size();

        return StaffOverviewResponse.builder()
                .totalStaffs(total)
                .activeStaffs(active)
                .inactiveStaffs(inactive)
                .blockedStaffs(blocked)
                .build();
    }

    // ===== Helpers =====

    private Manager validateManagerExists(Integer managerId) {
        return managerRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found with id: " + managerId));
    }

    private OperatorStaff validateStaffBelongsToManager(Integer managerId, Integer operatorId) {
        return operatorStaffRepository.findByOperatorIdAndManagerManagerId(operatorId, managerId)
                .orElseThrow(() -> new RuntimeException("Staff not found or does not belong to this manager"));
    }

    private OperatorStaffResponse convertToStaffResponse(OperatorStaff staff) {
        Users user = staff.getUsers();
        return OperatorStaffResponse.builder()
                .operatorId(staff.getOperatorId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .gender(user.getGender())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .totalBookings(staff.getBookings() != null ? (long) staff.getBookings().size() : 0L)
                .totalFeedbacks(staff.getFeedbacks() != null ? (long) staff.getFeedbacks().size() : 0L)
                .totalChatbotLogs(staff.getChatbotLogs() != null ? (long) staff.getChatbotLogs().size() : 0L)
                .build();
    }
    @Override
    @Transactional(readOnly = true)
    public OperatorStaffResponse getStaffDetails(Integer managerId, Integer operatorId) {
        OperatorStaff staff = validateStaffBelongsToManager(managerId, operatorId);
        return convertToStaffResponse(staff);
    }

}
