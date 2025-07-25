package org.example.operatormanagementsystem.managestaff_yen.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.operatormanagementsystem.entity.*;
import org.example.operatormanagementsystem.enumeration.UserGender;
import org.example.operatormanagementsystem.enumeration.UserStatus;
import org.example.operatormanagementsystem.managestaff_yen.dto.request.*;
import org.example.operatormanagementsystem.managestaff_yen.dto.response.*;
import org.example.operatormanagementsystem.managestaff_yen.repository.*;
import org.example.operatormanagementsystem.managestaff_yen.service.ExcelExportService;
import org.example.operatormanagementsystem.managestaff_yen.service.StaffManagementService;
import org.example.operatormanagementsystem.transportunit.repository.ManagerRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;
import java.util.Map;
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
    private final ExcelExportService excelExportService;

    @Override
    @Transactional(readOnly = true)
    public StaffListResponse viewStaffInformation(Integer managerId, int page, int size, String sortBy, String sortDir) {
        validateManagerExists(managerId);
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<OperatorStaff> staffPage = operatorStaffRepository.findByManagerManagerId(managerId, pageable);
        return mapToStaffListResponse(staffPage);
    }

    @Override
    @Transactional(readOnly = true)
    public StaffListResponse searchStaff(Integer managerId, String searchTerm, String status, String gender, int page, int size) {
        validateManagerExists(managerId);
        Pageable pageable = PageRequest.of(page, size);

        UserStatus statusEnum = parseEnum(UserStatus.class, status);
        UserGender genderEnum = parseEnum(UserGender.class, gender);
        String search = (searchTerm != null && !searchTerm.trim().isEmpty()) ? searchTerm.trim() : null;

        Page<OperatorStaff> staffPage = operatorStaffRepository.searchStaffWithFilters(
                managerId, search, statusEnum, genderEnum, pageable
        );
        return mapToStaffListResponse(staffPage);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportStaffToExcel(Integer managerId, ExportStaffRequest request) throws IOException {
        validateManagerExists(managerId);

        Page<OperatorStaff> page = operatorStaffRepository.searchStaffWithFilters(
                managerId,
                (request.getSearchTerm() != null && !request.getSearchTerm().trim().isEmpty()) ? request.getSearchTerm().trim() : null,
                request.getStatusFilter(),
                request.getGenderFilter(),
                Pageable.unpaged()
        );
        List<OperatorStaff> allStaff = page.getContent();
        allStaff = applySorting(allStaff, request.getSortBy(), request.getSortDir());

        List<OperatorStaffResponse> staffResponses = allStaff.stream()
                .map(this::convertToStaffResponse)
                .collect(Collectors.toList());

        StaffOverviewResponse overview = request.getIncludeStatistics() ? getStaffOverview(managerId) : null;

        return excelExportService.exportStaffToExcel(staffResponses, overview, managerId, request.getIncludeStatistics());
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
        user.setStatus(UserStatus.BLOCKED);
        usersRepository.save(user);
    }

    @Override
    public void feedbackToStaff(Integer managerId, Integer operatorId, ManagerFeedbackRequest request) {
        Manager manager = validateManagerExists(managerId);
        OperatorStaff staff = validateStaffBelongsToManager(managerId, operatorId);

        ManagerFeedbackToStaff feedback = ManagerFeedbackToStaff.builder()
                .manager(manager)
                .operatorStaff(staff)
                .feedbackContent(request.getContent())
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

    @Override
    @Transactional(readOnly = true)
    public OperatorStaffResponse getStaffDetails(Integer managerId, Integer operatorId) {
        OperatorStaff staff = validateStaffBelongsToManager(managerId, operatorId);
        return convertToStaffResponse(staff);
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
                .build();
    }

    private List<OperatorStaff> applySorting(List<OperatorStaff> staffList, String sortBy, String sortDir) {
        Comparator<OperatorStaff> comparator;
        switch (sortBy) {
            case "operatorId" -> comparator = Comparator.comparing(OperatorStaff::getOperatorId);
            case "users.fullName" -> comparator = Comparator.comparing(staff -> staff.getUsers().getFullName(), String.CASE_INSENSITIVE_ORDER);
            case "users.username" -> comparator = Comparator.comparing(staff -> staff.getUsers().getUsername(), String.CASE_INSENSITIVE_ORDER);
            case "users.email" -> comparator = Comparator.comparing(staff -> staff.getUsers().getEmail(), String.CASE_INSENSITIVE_ORDER);
            case "users.createdAt" -> comparator = Comparator.comparing(staff -> staff.getUsers().getCreatedAt(), Comparator.nullsLast(Comparator.naturalOrder()));
            default -> comparator = Comparator.comparing(staff -> staff.getUsers().getFullName(), String.CASE_INSENSITIVE_ORDER);
        }

        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }

        return staffList.stream().sorted(comparator).collect(Collectors.toList());
    }

    private <T extends Enum<T>> T parseEnum(Class<T> enumClass, String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid value for " + enumClass.getSimpleName() + ": " + value);
        }
    }

    private StaffListResponse mapToStaffListResponse(Page<OperatorStaff> staffPage) {
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
    public StaffPerformanceOverviewResponse getStaffPerformanceOverview(Integer managerId) {
        validateManagerExists(managerId);
        List<OperatorStaff> staffList = operatorStaffRepository.findByManagerManagerId(managerId);

        long total = staffList.size();
        long active = staffList.stream().filter(s -> s.getUsers().getStatus() == UserStatus.ACTIVE).count();
        long inactive = staffList.stream().filter(s -> s.getUsers().getStatus() == UserStatus.INACTIVE).count();
        long blocked = staffList.stream().filter(s -> s.getUsers().getStatus() == UserStatus.BLOCKED).count();

        // Thống kê theo tháng tạo
        Map<String, Long> monthlyCreatedStats = staffList.stream()
                .filter(s -> s.getUsers().getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        s -> s.getUsers().getCreatedAt().format(DateTimeFormatter.ofPattern("MM/yyyy")),
                        Collectors.counting()
                ));

        // Phân bố trạng thái
        Map<String, Long> statusDistribution = staffList.stream()
                .map(s -> s.getUsers().getStatus().name())
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        // Top booking
        List<StaffPerformanceOverviewResponse.OperatorPerformanceDTO> topBooking = staffList.stream()
                .map(s -> StaffPerformanceOverviewResponse.OperatorPerformanceDTO.builder()
                        .operatorId(s.getOperatorId())
                        .fullName(s.getUsers().getFullName())
                        .totalBookings(s.getBookings() != null ? s.getBookings().size() : 0)
                        .totalFeedbacks(s.getFeedbacks() != null ? s.getFeedbacks().size() : 0)
                        .build())
                .sorted(Comparator.comparingLong(StaffPerformanceOverviewResponse.OperatorPerformanceDTO::getTotalBookings).reversed())
                .limit(5)
                .collect(Collectors.toList());

        // Top feedback
        List<StaffPerformanceOverviewResponse.OperatorPerformanceDTO> topFeedback = staffList.stream()
                .map(s -> StaffPerformanceOverviewResponse.OperatorPerformanceDTO.builder()
                        .operatorId(s.getOperatorId())
                        .fullName(s.getUsers().getFullName())
                        .totalBookings(s.getBookings() != null ? s.getBookings().size() : 0)
                        .totalFeedbacks(s.getFeedbacks() != null ? s.getFeedbacks().size() : 0)
                        .build())
                .sorted(Comparator.comparingLong(StaffPerformanceOverviewResponse.OperatorPerformanceDTO::getTotalFeedbacks).reversed())
                .limit(5)
                .collect(Collectors.toList());

        return StaffPerformanceOverviewResponse.builder()
                .totalStaffs(total)
                .activeStaffs(active)
                .inactiveStaffs(inactive)
                .blockedStaffs(blocked)
                .monthlyCreatedStats(monthlyCreatedStats)
                .statusDistribution(statusDistribution)
                .topBookingStaffs(topBooking)
                .topFeedbackStaffs(topFeedback)
                .build();
    }

}
