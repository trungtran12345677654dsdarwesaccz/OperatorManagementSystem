package org.example.operatormanagementsystem.managestaff_yen.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.operatormanagementsystem.managestaff_yen.dto.request.ManagerFeedbackRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.request.UpdateStaffRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.response.OperatorStaffResponse;
import org.example.operatormanagementsystem.managestaff_yen.dto.response.StaffListResponse;
import org.example.operatormanagementsystem.managestaff_yen.dto.response.StaffOverviewResponse;
import org.example.operatormanagementsystem.entity.Manager;
import org.example.operatormanagementsystem.entity.ManagerFeedbackToStaff;
import org.example.operatormanagementsystem.entity.OperatorStaff;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.enumeration.UserStatus;
import org.example.operatormanagementsystem.managestaff_yen.repository.ManagerFeedbackToStaffRepository;
import org.example.operatormanagementsystem.managestaff_yen.repository.ManagerRepository;
import org.example.operatormanagementsystem.managestaff_yen.repository.OperatorStaffRepository;
import org.example.operatormanagementsystem.managestaff_yen.repository.UsersRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StaffManagementService {

    private final OperatorStaffRepository operatorStaffRepository;
    private final ManagerRepository managerRepository;
    private final UsersRepository usersRepository;
    private final ManagerFeedbackToStaffRepository managerFeedbackToStaffRepository;

    /**
     * Xem danh sách tất cả staff của manager
     */
    @Transactional(readOnly = true)
    public StaffListResponse viewStaffInformation(Integer managerId, int page, int size, String sortBy, String sortDir) {
        log.info("Viewing staff information for manager: {}", managerId);

        // Validate manager exists
        validateManagerExists(managerId);

        // Create pageable with sorting
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Get staff page
        Page<OperatorStaff> staffPage = operatorStaffRepository.findByManagerManagerId(managerId, pageable);

        // Convert to response DTOs
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

    /**
     * Tìm kiếm staff theo từ khóa
     */
    @Transactional(readOnly = true)
    public StaffListResponse searchStaff(Integer managerId, String searchTerm, int page, int size) {
        log.info("Searching staff for manager: {} with term: {}", managerId, searchTerm);

        validateManagerExists(managerId);

        Pageable pageable = PageRequest.of(page, size);
        Page<OperatorStaff> staffPage = operatorStaffRepository.searchStaffByManagerAndTerm(
                managerId, searchTerm, pageable);

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

    /**
     * Cập nhật thông tin staff
     */
    public OperatorStaffResponse updateStaffInformation(Integer managerId, Integer operatorId, UpdateStaffRequest request) {
        log.info("Updating staff information for operator: {} by manager: {}", operatorId, managerId);

        // Validate manager and staff relationship
        OperatorStaff staff = validateStaffBelongsToManager(managerId, operatorId);

        // Update user information
        Users user = staff.getUsers();
        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setGender(request.getGender());
        user.setStatus(request.getStatus());

        // Save changes
        usersRepository.save(user);

        log.info("Successfully updated staff information for operator: {}", operatorId);
        return convertToStaffResponse(staff);
    }

    /**
     * Block/Delete staff account (soft delete by changing status)
     */
    public void blockOrDeleteStaffAccount(Integer managerId, Integer operatorId, boolean permanentDelete) {
        log.info("Processing staff account action for operator: {} by manager: {}, permanent: {}",
                operatorId, managerId, permanentDelete);

        OperatorStaff staff = validateStaffBelongsToManager(managerId, operatorId);

        if (permanentDelete) {
            Users user = staff.getUsers(); // lấy bản ghi user liên quan

            // Xoá bản ghi liên kết trước
            operatorStaffRepository.delete(staff);

            // Sau đó xoá luôn user
            usersRepository.delete(user);

            log.info("Permanently deleted staff account and user: {}", operatorId);
        }
        else {
            // Soft delete - change status to BLOCKED
            Users user = staff.getUsers();
            user.setStatus(UserStatus.BLOCKED);
            usersRepository.save(user);
            log.info("Blocked staff account for operator: {}", operatorId);
        }
    }

    /**
     * Gửi feedback cho staff
     */
    public void feedbackToStaff(Integer managerId, Integer operatorId, ManagerFeedbackRequest request) {
        log.info("Sending feedback from manager: {} to operator: {}", managerId, operatorId);

        Manager manager = validateManagerExists(managerId);
        OperatorStaff staff = validateStaffBelongsToManager(managerId, operatorId);

        // Create feedback entity
        ManagerFeedbackToStaff feedback = ManagerFeedbackToStaff.builder()
                .manager(manager)
                .operatorStaff(staff)
                .content(request.getContent())
                .rating(request.getRating())
                .createdAt(LocalDateTime.now())
                .build();

        managerFeedbackToStaffRepository.save(feedback);
        log.info("Successfully sent feedback from manager: {} to operator: {}", managerId, operatorId);
    }

    /**
     * Lấy thống kê tổng quan về staff
     */
    @Transactional(readOnly = true)
    public StaffOverviewResponse getStaffOverview(Integer managerId) {
        log.info("Getting staff overview for manager: {}", managerId);

        validateManagerExists(managerId);

        long totalStaffs = operatorStaffRepository.countByManagerManagerId(managerId);
        long activeStaffs = operatorStaffRepository.findByManagerManagerIdAndUsersStatus(managerId, UserStatus.ACTIVE).size();
        long inactiveStaffs = operatorStaffRepository.findByManagerManagerIdAndUsersStatus(managerId, UserStatus.INACTIVE).size();
        long blockedStaffs = operatorStaffRepository.findByManagerManagerIdAndUsersStatus(managerId, UserStatus.BLOCKED).size();

        return StaffOverviewResponse.builder()
                .totalStaffs(totalStaffs)
                .activeStaffs(activeStaffs)
                .inactiveStaffs(inactiveStaffs)
                .blockedStaffs(blockedStaffs)
                .build();
    }

    // Helper methods
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
}