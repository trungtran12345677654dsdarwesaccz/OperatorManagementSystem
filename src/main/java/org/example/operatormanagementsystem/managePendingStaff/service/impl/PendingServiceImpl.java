package org.example.operatormanagementsystem.managePendingStaff.service.impl;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.operatormanagementsystem.config.JwtUtil;
import org.example.operatormanagementsystem.dto.response.UserResponse;
import org.example.operatormanagementsystem.entity.UserApprovalHistory;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.enumeration.ApprovalStatus;
import org.example.operatormanagementsystem.enumeration.UserStatus;
import org.example.operatormanagementsystem.managePendingStaff.dto.request.ApprovalHistoryFilterRequest;
import org.example.operatormanagementsystem.managePendingStaff.dto.request.PendingUserFilterRequest;
import org.example.operatormanagementsystem.managePendingStaff.dto.response.UserApprovalHistoryResponse;
import org.example.operatormanagementsystem.managePendingStaff.repository.UserApprovalHistoryRepository;
import org.example.operatormanagementsystem.managePendingStaff.service.PendingService;
import org.example.operatormanagementsystem.repository.UserRepository;
import org.example.operatormanagementsystem.service.EmailService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class PendingServiceImpl implements PendingService {

    private final HttpServletRequest request;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final UserApprovalHistoryRepository userApprovalHistoryRepository;

    @Override
    @Transactional
    public Users updateStatusByManager(String email, UserStatus newStatus) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        UserStatus oldStatus = user.getStatus();

        String token = request.getHeader("Authorization").substring(7);
        String managerEmail = jwtUtil.extractUsername(token);
        Users manager = userRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Manager not found with email: " + managerEmail));

        switch (newStatus) {
            case ACTIVE -> {
                if (oldStatus == UserStatus.PENDING_APPROVAL || oldStatus == UserStatus.INACTIVE) {
                    user.setStatus(UserStatus.ACTIVE);
                    userRepository.save(user);
                    saveApprovalHistory(user, manager, oldStatus, UserStatus.ACTIVE, ApprovalStatus.APPROVED, "Approved to ACTIVE");
                    try {
                        emailService.sendStatusChangeNotification(user.getEmail(), UserStatus.ACTIVE);
                    } catch (MessagingException e) {
                        System.err.println("Failed to send activation email to " + user.getEmail() + ": " + e.getMessage());
                    }
                    return user;
                }
                throw new IllegalStateException("Cannot activate user from current status: " + oldStatus);
            }
            case REJECTED -> {
                if (oldStatus == UserStatus.PENDING_APPROVAL) {
                    user.setStatus(UserStatus.INACTIVE);
                    userRepository.save(user);
                    saveApprovalHistory(user, manager, oldStatus, UserStatus.INACTIVE, ApprovalStatus.REJECTED, "Rejected from PENDING_APPROVAL to INACTIVE");
                    try {
                        emailService.sendStatusChangeNotification(user.getEmail(), UserStatus.INACTIVE);
                    } catch (MessagingException e) {
                        System.err.println("Failed to send rejection email to " + user.getEmail() + ": " + e.getMessage());
                    }
                    return user;
                }
                throw new IllegalStateException("Cannot reject user from current status: " + oldStatus);
            }
            case INACTIVE -> {
                if (oldStatus == UserStatus.ACTIVE) {
                    user.setStatus(UserStatus.INACTIVE);
                    userRepository.save(user);
                    saveApprovalHistory(user, manager, oldStatus, UserStatus.INACTIVE, ApprovalStatus.REJECTED, "Set to INACTIVE from ACTIVE");
                    try {
                        emailService.sendStatusChangeNotification(user.getEmail(), UserStatus.INACTIVE);
                    } catch (MessagingException e) {
                        System.err.println("Failed to send inactive email to " + user.getEmail() + ": " + e.getMessage());
                    }
                    return user;
                }
                throw new IllegalStateException("Cannot set to INACTIVE from current status: " + oldStatus);
            }
            default -> throw new IllegalArgumentException("Invalid status update request: " + newStatus);
        }
    }

    private void saveApprovalHistory(Users user, Users approvedBy, UserStatus fromStatus, UserStatus toStatus, ApprovalStatus status, String note) {
        String ip = request.getRemoteAddr();
        UserApprovalHistory history = UserApprovalHistory.builder()
                .user(user)
                .approvedBy(approvedBy)
                .status(status)
                .note(note)
                .approvedAt(LocalDateTime.now())
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .approvedByIp(ip)
                .build();
        userApprovalHistoryRepository.save(history);
    }

    @Override
    public Page<UserResponse> getUsersByStatus(UserStatus status, Pageable pageable) {
        return userRepository.findByStatus(status, pageable)
                .map(this::toUserResponse);
    }

    @Override
    public Page<UserResponse> getUsersNeedingManagerAction(Pageable pageable) {
        return userRepository.findByStatus(UserStatus.PENDING_APPROVAL, pageable)
                .map(this::toUserResponse);
    }

    @Override
    public Page<UserResponse> searchPendingUsers(PendingUserFilterRequest request, Pageable pageable) {
        Page<Users> page = userRepository.filterPendingUsers(
                request.getEmail(),
                request.getUsername(),
                request.getFullName(),
                request.getGender(),
                request.getAddress(),
                pageable
        );

        return page.map(this::toUserResponse);
    }


    @Override
    public UserResponse getUserDetailsForManager(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return toUserResponse(user);
    }

    @Override
    public List<UserApprovalHistoryResponse> getApprovalHistoryByUser(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        List<UserApprovalHistory> historyList = userApprovalHistoryRepository.findByUserOrderByApprovedAtDesc(user);
        return historyList.stream().map(this::toApprovalHistoryResponse).toList();
    }

    @Override
    public Page<UserApprovalHistoryResponse> getApprovalHistoryFiltered(ApprovalHistoryFilterRequest request, Pageable pageable) {
        Page<UserApprovalHistory> historyPage = userApprovalHistoryRepository.searchApprovalHistory(
                request.getUserEmail(),
                request.getApprovedByEmail(),
                request.getStatus(),
                request.getFromDate(),
                request.getToDate(),
                pageable
        );
        return historyPage.map(this::toApprovalHistoryResponse);
    }

    @Override
    public List<UserApprovalHistoryResponse> getAllApprovalHistories() {
        List<UserApprovalHistory> historyList = userApprovalHistoryRepository.findAll(Sort.by(Sort.Direction.DESC, "approvedAt"));
        return historyList.stream().map(this::toApprovalHistoryResponse).toList();
    }

    @Override
    public Page<UserApprovalHistoryResponse> getAllApprovalHistories(Pageable pageable) {
        Page<UserApprovalHistory> historyPage = userApprovalHistoryRepository.findAll(pageable);
        return historyPage.map(this::toApprovalHistoryResponse);
    }

    private UserResponse toUserResponse(Users user) {
        return UserResponse.builder()
                .userName(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .gender(user.getGender().toString())
                .address(user.getAddress())
                .status(user.getStatus())
                .build();
    }

    private UserApprovalHistoryResponse toApprovalHistoryResponse(UserApprovalHistory history) {
        return UserApprovalHistoryResponse.builder()
                .userEmail(history.getUser().getEmail())
                .approvedByEmail(history.getApprovedBy() != null ? history.getApprovedBy().getEmail() : null)
                .status(history.getStatus())
                .note(history.getNote())
                .approvedAt(history.getApprovedAt())
                .fromStatus(history.getFromStatus())
                .toStatus(history.getToStatus())
                .approvedByIp(history.getApprovedByIp())
                .build();
    }
}