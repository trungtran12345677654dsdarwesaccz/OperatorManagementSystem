package org.example.operatormanagementsystem.dashboardstaff.service.impl;

import org.example.operatormanagementsystem.ManageHungBranch.repository.PaymentRepository;
import org.example.operatormanagementsystem.dashboardstaff.dto.request.DashboardStaffRequest;
import org.example.operatormanagementsystem.dashboardstaff.dto.response.DashboardStaffResponse;
import org.example.operatormanagementsystem.dashboardstaff.dto.response.RecentActivityResponse;
import org.example.operatormanagementsystem.dashboardstaff.repository.PositionRepository;
import org.example.operatormanagementsystem.dashboardstaff.service.DashboardStaffService;
import org.example.operatormanagementsystem.entity.Booking;
import org.example.operatormanagementsystem.entity.Payment;
import org.example.operatormanagementsystem.entity.Position;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.managecustomerorderbystaff.repository.BookingRepository;
import org.example.operatormanagementsystem.managecustomerorderbystaff.repository.CustomerRepository;
import org.example.operatormanagementsystem.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardStaffServiceImpl implements DashboardStaffService {

    private final PositionRepository positionRepository;
    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    public DashboardStaffServiceImpl(PositionRepository positionRepository,
                                     BookingRepository bookingRepository,
                                     CustomerRepository customerRepository,
                                     UserRepository userRepository) {
        this.positionRepository = positionRepository;
        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void addPosition(DashboardStaffRequest request) {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = principal instanceof UserDetails ? ((UserDetails) principal).getUsername() : principal.toString();
            Users currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + username));

            Integer requestedUserId = request.getUserId();
            boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin && !currentUser.getId().equals(requestedUserId)) {
                throw new RuntimeException("Bạn không có quyền thêm/cập nhật chức vụ cho người dùng khác.");
            }

            Users targetUser = userRepository.findById(requestedUserId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + requestedUserId));

            boolean isNewPosition = positionRepository.findByUserId(requestedUserId)
                    .stream()
                    .findFirst()
                    .isEmpty();
            Position position = isNewPosition ? new Position() : positionRepository.findByUserId(requestedUserId)
                    .stream()
                    .findFirst()
                    .get();

            position.setTitle(request.getTitle());
            position.setSecondaryTitle(request.getSecondaryTitle());
            position.setDescription(request.getDescription());
            position.setBaseSalary(request.getBaseSalary());

            String status = request.getStatus();
            if ("Hoạt động".equalsIgnoreCase(status) || "đang hoạt động".equalsIgnoreCase(status)) {
                status = "ACTIVE";
            } else if ("Tạm ngưng".equalsIgnoreCase(status) || "tạm ngưng".equalsIgnoreCase(status)) {
                status = "INACTIVE";
            } else if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
                throw new RuntimeException("Trạng thái không hợp lệ: " + status + ". Chỉ chấp nhận 'Hoạt động' hoặc 'Tạm ngưng'.");
            }
            position.setStatus(status);

            if (isNewPosition) {
                position.setCreatedAt(LocalDateTime.now());
                position.setUser(targetUser);
            }

            positionRepository.save(position);
            System.out.println((isNewPosition ? "Thêm" : "Cập nhật") + " chức vụ thành công: " + request.getTitle() + " cho người dùng ID: " + requestedUserId);
        } catch (Exception e) {
            System.out.println("Lỗi khi thêm/cập nhật chức vụ: " + e.getMessage());
            throw new RuntimeException("Lỗi khi thêm/cập nhật chức vụ: " + e.getMessage());
        }
    }

    @Override
    public DashboardStaffResponse getDashboardStats() {
        try {
            DashboardStaffResponse stats = new DashboardStaffResponse();
            stats.setPendingOrders((int) bookingRepository.countByStatus("PENDING"));
            stats.setNewCustomers((int) customerRepository.countByCreatedAtAfter(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).minusDays(30))); // Khách hàng mới trong 30 ngày
            stats.setPendingCustomers((int) customerRepository.countByStatus("PENDING")); // Khách hàng chờ

            System.out.println("Thống kê: Pending Orders = " + stats.getPendingOrders());
            return stats;
        } catch (Exception e) {
            System.out.println("Lỗi khi lấy thống kê: " + e.getMessage());
            throw new RuntimeException("Lỗi khi lấy thống kê: " + e.getMessage());
        }
    }

    @Override
    public List<RecentActivityResponse> getRecentActivities() {
        try {
            List<RecentActivityResponse> activities = new ArrayList<>();
            // Thêm hoạt động từ Booking
            List<Booking> recentBookings = bookingRepository.findAll();
            for (Booking booking : recentBookings) {
                RecentActivityResponse response = new RecentActivityResponse();
                response.setAction("Cập nhật đơn hàng #" + booking.getBookingId());
                response.setTimeAgo("vừa xong");
                activities.add(response);
            }


            // Thêm hoạt động từ Position
            List<Position> recentPositions = positionRepository.findAll();
            for (Position position : recentPositions) {
                RecentActivityResponse response = new RecentActivityResponse();
                response.setAction("Cập nhật chức vụ '" + position.getTitle() + "' cho ID: " + position.getUser().getId());
                response.setTimeAgo("2 giờ trước");
                activities.add(response);
            }

            // Thêm hoạt động từ User
            List<Users> recentUsers = userRepository.findAll();
            for (Users user : recentUsers) {
                RecentActivityResponse response = new RecentActivityResponse();
                response.setAction("Cập nhật thông tin người dùng '" + user.getUsername() + "'");
                response.setTimeAgo("3 giờ trước");
                activities.add(response);
            }

            // Giới hạn số lượng hoạt động trả về là 4
            if (activities.size() > 4) {
                return activities.subList(0, 4);
            }
            System.out.println("Lấy " + activities.size() + " hoạt động gần đây");
            return activities;
        } catch (Exception e) {
            System.out.println("Lỗi khi lấy hoạt động: " + e.getMessage());
            throw new RuntimeException("Lỗi khi lấy hoạt động: " + e.getMessage());
        }
    }
}