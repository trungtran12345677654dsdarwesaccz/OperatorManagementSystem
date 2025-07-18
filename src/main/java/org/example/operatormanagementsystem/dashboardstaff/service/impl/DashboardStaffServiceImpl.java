package org.example.operatormanagementsystem.dashboardstaff.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.ManageHungBranch.repository.PaymentRepository;
import org.example.operatormanagementsystem.dashboardstaff.dto.request.DashboardStaffRequest;
import org.example.operatormanagementsystem.dashboardstaff.dto.response.DashboardStaffResponse;
import org.example.operatormanagementsystem.dashboardstaff.dto.response.MonthlyRevenueResponse;
import org.example.operatormanagementsystem.dashboardstaff.dto.response.PerformanceDataResponse;
import org.example.operatormanagementsystem.dashboardstaff.dto.response.DetailDataResponse;
import org.example.operatormanagementsystem.dashboardstaff.dto.response.TransportDataResponse;
import org.example.operatormanagementsystem.dashboardstaff.dto.response.RankingDataResponse;
import org.example.operatormanagementsystem.dashboardstaff.dto.response.RecentActivityResponse;
import org.example.operatormanagementsystem.dashboardstaff.dto.response.TeamRankingResponse;
import org.example.operatormanagementsystem.dashboardstaff.dto.response.AchievementResponse;
import org.example.operatormanagementsystem.dashboardstaff.service.DashboardStaffService;
import org.example.operatormanagementsystem.entity.Booking;
import org.example.operatormanagementsystem.entity.Feedback;
import org.example.operatormanagementsystem.entity.OperatorStaff;
import org.example.operatormanagementsystem.entity.Position;
import org.example.operatormanagementsystem.entity.Revenue;
import org.example.operatormanagementsystem.entity.TransportUnit;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.managecustomerorderbystaff.repository.BookingRepository;
import org.example.operatormanagementsystem.managecustomerorderbystaff.repository.CustomerRepository;
import org.example.operatormanagementsystem.repository.UserRepository;
import org.example.operatormanagementsystem.repository.RevenueRepository;
import org.example.operatormanagementsystem.transportunit.repository.TransportUnitRepository;
import org.example.operatormanagementsystem.managestaff_yen.repository.OperatorStaffRepository;
import org.example.operatormanagementsystem.dashboardstaff.repository.FeedbackRepository;
import org.example.operatormanagementsystem.dashboardstaff.repository.PositionRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardStaffServiceImpl implements DashboardStaffService {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,### đ");

    private final PositionRepository positionRepository;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final RevenueRepository revenueRepository;
    private final TransportUnitRepository transportUnitRepository;
    private final OperatorStaffRepository operatorStaffRepository;
    private final FeedbackRepository feedbackRepository;

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

            Optional<Position> existingPosition = positionRepository.findByUserId(requestedUserId)
                    .stream()
                    .findFirst();

            Position position = existingPosition.orElseGet(Position::new);

            position.setTitle(request.getTitle());
            position.setSecondaryTitle(request.getSecondaryTitle());
            position.setDescription(request.getDescription());

            String status = request.getStatus();
            if ("Hoạt động".equalsIgnoreCase(status) || "đang hoạt động".equalsIgnoreCase(status)) {
                status = "ACTIVE";
            } else if ("Tạm ngưng".equalsIgnoreCase(status) || "tạm ngưng".equalsIgnoreCase(status)) {
                status = "INACTIVE";
            } else if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
                throw new RuntimeException("Trạng thái không hợp lệ: " + status + ". Chỉ chấp nhận 'Hoạt động' hoặc 'Tạm ngưng'.");
            }
            position.setStatus(status);

            if (existingPosition.isEmpty()) {
                position.setCreatedAt(LocalDateTime.now());
                position.setUser(targetUser);
            }

            positionRepository.save(position);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi thêm/cập nhật chức vụ: " + e.getMessage());
        }
    }

    @Override
    public DashboardStaffResponse getDashboardStats() {
        try {
            DashboardStaffResponse stats = new DashboardStaffResponse();
            stats.setNewReceipts((int) bookingRepository.countByStatus("COMPLETED"));
            stats.setPendingOrders((int) bookingRepository.countByStatus("PENDING"));
            stats.setNewCustomers((int) customerRepository.countByCreatedAtAfter(
                    LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).minusDays(30)));
            return stats;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lấy thống kê: " + e.getMessage());
        }
    }

    @Override
    public List<RecentActivityResponse> getRecentActivities() {
        try {
            List<RecentActivityResponse> activities = new ArrayList<>();
            List<Booking> recentBookings = bookingRepository.findAll();
            if (recentBookings != null) {
                for (Booking booking : recentBookings) {
                    RecentActivityResponse response = new RecentActivityResponse();
                    response.setAction("Cập nhật đơn hàng #" + (booking.getBookingId() != null ? booking.getBookingId() : "unknown"));
                    response.setTimeAgo("vừa xong");
                    activities.add(response);
                }
            }

            List<Booking> pendingPayments = bookingRepository.findAll();
            if (pendingPayments != null) {
                for (Booking booking : pendingPayments) {
                    if ("INCOMPLETED".equals(booking.getPaymentStatus())) {
                        RecentActivityResponse response = new RecentActivityResponse();
                        response.setAction("Xử lý biên lai #" + (booking.getBookingId() != null ? booking.getBookingId() : "unknown"));
                        response.setTimeAgo("1 giờ trước");
                        activities.add(response);
                    }
                }
            }

            if (activities.size() > 4) {
                return activities.subList(0, 4);
            }
            return activities;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<MonthlyRevenueResponse> getMonthlyRevenue(String year, String unit) {
        try {
            List<Revenue> revenues = revenueRepository.findAll();
            LocalDate startDate = year.equals("Tất cả") ? LocalDate.of(2000, 1, 1) : LocalDate.of(Integer.parseInt(year), 1, 1);
            LocalDate endDate = year.equals("Tất cả") ? LocalDate.now() : LocalDate.of(Integer.parseInt(year), 12, 31);

            revenues = revenues.stream()
                    .filter(r -> !r.getDate().isBefore(startDate) && !r.getDate().isAfter(endDate))
                    .filter(r -> unit.equals("Tất cả") ||
                            (r.getBeneficiaryType().equals("TRANSPORT_UNIT") &&
                                    transportUnitRepository.findById(Integer.parseInt(r.getBeneficiaryId().toString()))
                                            .map(tu -> tu.getNameCompany().equals(unit))
                                            .orElse(false)))
                    .collect(Collectors.toList());

            Map<String, Map<String, BigDecimal>> monthlyData = revenues.stream()
                    .collect(Collectors.groupingBy(
                            r -> r.getDate().format(DateTimeFormatter.ofPattern("M/yyyy")),
                            Collectors.groupingBy(
                                    r -> transportUnitRepository.findById(Integer.parseInt(r.getBeneficiaryId().toString()))
                                            .map(TransportUnit::getNameCompany)
                                            .orElse("Unknown"),
                                    Collectors.reducing(BigDecimal.ZERO, Revenue::getAmount, BigDecimal::add)
                            )
                    ));

            List<MonthlyRevenueResponse> result = new ArrayList<>();
            monthlyData.forEach((month, unitMap) -> {
                MonthlyRevenueResponse response = new MonthlyRevenueResponse();
                response.setMonth(month);
                response.setChuyenNha24H(unitMap.getOrDefault("Chuyển Nhà 24H", BigDecimal.ZERO));
                response.setDvChuyenNhaSaiGon(unitMap.getOrDefault("DV Chuyển Nhà Sài Gòn", BigDecimal.ZERO));
                response.setChuyenNhaMinhAnh(unitMap.getOrDefault("Chuyển Nhà Minh Anh", BigDecimal.ZERO));
                result.add(response);
            });

            return result.stream()
                    .sorted(Comparator.comparing(r -> {
                        String[] parts = r.getMonth().split("/");
                        return LocalDate.of(Integer.parseInt(parts[1]), Integer.parseInt(parts[0]), 1);
                    }))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<PerformanceDataResponse> getPerformanceData(String year, String unit) {
        try {
            List<Booking> bookings = bookingRepository.findAll();
            LocalDate startDate = year.equals("Tất cả") ? LocalDate.of(2000, 1, 1) : LocalDate.of(Integer.parseInt(year), 1, 1);
            LocalDate endDate = year.equals("Tất cả") ? LocalDate.now() : LocalDate.of(Integer.parseInt(year), 12, 31);

            bookings = bookings.stream()
                    .filter(b -> {
                        LocalDate bookingDate = b.getDeliveryDate() != null ? b.getDeliveryDate().toLocalDate() : null;
                        return bookingDate != null && !bookingDate.isBefore(startDate) && !bookingDate.isAfter(endDate);
                    })
                    .filter(b -> unit.equals("Tất cả") || b.getTransportUnit().getNameCompany().equals(unit))
                    .collect(Collectors.toList());

            Map<String, List<Booking>> monthlyBookings = bookings.stream()
                    .collect(Collectors.groupingBy(
                            b -> b.getDeliveryDate() != null ? b.getDeliveryDate().toLocalDate().format(DateTimeFormatter.ofPattern("M/yyyy")) : "Unknown"
                    ));

            List<PerformanceDataResponse> result = new ArrayList<>();
            monthlyBookings.forEach((month, bookingList) -> {
                PerformanceDataResponse response = new PerformanceDataResponse();
                response.setMonth(month);
                response.setDungHan((int) bookingList.stream()
                        .filter(b -> "COMPLETED".equals(b.getStatus()) && b.getDeliveryDate() != null && !b.getDeliveryDate().toLocalDate().isAfter(LocalDate.now()))
                        .count());
                response.setHuy((int) bookingList.stream().filter(b -> "CANCELLED".equals(b.getStatus())).count());
                response.setTre((int) bookingList.stream()
                        .filter(b -> "COMPLETED".equals(b.getStatus()) && b.getDeliveryDate() != null && b.getDeliveryDate().toLocalDate().isAfter(LocalDate.now()))
                        .count());
                result.add(response);
            });

            return result.stream()
                    .sorted(Comparator.comparing(r -> {
                        String[] parts = r.getMonth().split("/");
                        return LocalDate.of(Integer.parseInt(parts[1]), Integer.parseInt(parts[0]), 1);
                    }))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<DetailDataResponse> getDetailData(String year, String unit) {
        try {
            List<Booking> bookings = bookingRepository.findAll();
            LocalDate startDate = year.equals("Tất cả") ? LocalDate.of(2000, 1, 1) : LocalDate.of(Integer.parseInt(year), 1, 1);
            LocalDate endDate = year.equals("Tất cả") ? LocalDate.now() : LocalDate.of(Integer.parseInt(year), 12, 31);

            bookings = bookings.stream()
                    .filter(b -> {
                        LocalDate bookingDate = b.getDeliveryDate() != null ? b.getDeliveryDate().toLocalDate() : null;
                        return bookingDate != null && !bookingDate.isBefore(startDate) && !bookingDate.isAfter(endDate);
                    })
                    .filter(b -> unit.equals("Tất cả") || b.getTransportUnit().getNameCompany().equals(unit))
                    .collect(Collectors.toList());

            Map<String, Map<String, List<Booking>>> groupedData = bookings.stream()
                    .collect(Collectors.groupingBy(
                            b -> b.getDeliveryDate() != null ? b.getDeliveryDate().toLocalDate().format(DateTimeFormatter.ofPattern("M/yyyy")) : "Unknown",
                            Collectors.groupingBy(b -> b.getTransportUnit().getNameCompany())
                    ));

            List<DetailDataResponse> result = new ArrayList<>();
            groupedData.forEach((month, unitMap) -> {
                unitMap.forEach((unitName, bookingList) -> {
                    DetailDataResponse response = new DetailDataResponse();
                    response.setMonth(month);
                    response.setUnit(unitName);
                    response.setTrips(bookingList.size());
                    response.setRevenue(DECIMAL_FORMAT.format(bookingList.stream()
                            .map(b -> new BigDecimal(b.getTotal()))
                            .reduce(BigDecimal.ZERO, BigDecimal::add)));
                    response.setOnTime((int) bookingList.stream()
                            .filter(b -> "COMPLETED".equals(b.getStatus()) && b.getDeliveryDate() != null && !b.getDeliveryDate().toLocalDate().isAfter(LocalDate.now()))
                            .count());
                    response.setCancelled((int) bookingList.stream()
                            .filter(b -> "CANCELLED".equals(b.getStatus()))
                            .count());
                    response.setLate((int) bookingList.stream()
                            .filter(b -> "COMPLETED".equals(b.getStatus()) && b.getDeliveryDate() != null && b.getDeliveryDate().toLocalDate().isAfter(LocalDate.now()))
                            .count());
                    result.add(response);
                });
            });

            return result.stream()
                    .sorted(Comparator.comparing(r -> {
                        String[] parts = r.getMonth().split("/");
                        return LocalDate.of(Integer.parseInt(parts[1]), Integer.parseInt(parts[0]), 1);
                    }))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public TransportDataResponse getTransportData(String year, String unit) {
        try {
            List<Booking> bookings = bookingRepository.findAll();
            List<Revenue> revenues = revenueRepository.findAll();
            LocalDate startDate = year.equals("Tất cả") ? LocalDate.of(2000, 1, 1) : LocalDate.of(Integer.parseInt(year), 1, 1);
            LocalDate endDate = year.equals("Tất cả") ? LocalDate.now() : LocalDate.of(Integer.parseInt(year), 12, 31);

            bookings = bookings.stream()
                    .filter(b -> {
                        LocalDate bookingDate = b.getDeliveryDate() != null ? b.getDeliveryDate().toLocalDate() : null;
                        return bookingDate != null && !bookingDate.isBefore(startDate) && !bookingDate.isAfter(endDate);
                    })
                    .filter(b -> unit.equals("Tất cả") || b.getTransportUnit().getNameCompany().equals(unit))
                    .collect(Collectors.toList());

            revenues = revenues.stream()
                    .filter(r -> {
                        LocalDate revenueDate = r.getDate();
                        return !revenueDate.isBefore(startDate) && !revenueDate.isAfter(endDate);
                    })
                    .filter(r -> unit.equals("Tất cả") ||
                            (r.getBeneficiaryType().equals("TRANSPORT_UNIT") &&
                                    transportUnitRepository.findById(Integer.parseInt(r.getBeneficiaryId().toString()))
                                            .map(tu -> tu.getNameCompany().equals(unit))
                                            .orElse(false)))
                    .collect(Collectors.toList());

            TransportDataResponse response = new TransportDataResponse();
            response.setTotalShipments(bookings.size());
            response.setRevenue(DECIMAL_FORMAT.format(revenues.stream()
                    .map(r -> r.getAmount() != null ? r.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)));
            response.setDeliveryRate(bookings.isEmpty() ? 0 : (double) bookings.stream()
                    .filter(b -> "COMPLETED".equals(b.getStatus()) && b.getTransports() != null && b.getTransports().stream().anyMatch(t -> "ON_TIME".equals(t.getStatus())))
                    .count() / bookings.size() * 100);
            response.setTotalVolume(bookings.stream()
                    .flatMap(b -> b.getItems().stream())
                    .mapToDouble(item -> item.getWeight() != null ? item.getWeight().doubleValue() : 0)
                    .sum() / 1000);

            LocalDate prevStartDate = startDate.minusYears(1);
            LocalDate prevEndDate = endDate.minusYears(1);
            List<Booking> prevBookings = bookingRepository.findAll().stream()
                    .filter(b -> {
                        LocalDate bookingDate = b.getDeliveryDate() != null ? b.getDeliveryDate().toLocalDate() : null;
                        return bookingDate != null && !bookingDate.isBefore(prevStartDate) && !bookingDate.isAfter(prevEndDate);
                    })
                    .filter(b -> unit.equals("Tất cả") || b.getTransportUnit().getNameCompany().equals(unit))
                    .collect(Collectors.toList());
            List<Revenue> prevRevenues = revenueRepository.findAll().stream()
                    .filter(r -> {
                        LocalDate revenueDate = r.getDate();
                        return !revenueDate.isBefore(prevStartDate) && !revenueDate.isAfter(prevEndDate);
                    })
                    .filter(r -> unit.equals("Tất cả") ||
                            (r.getBeneficiaryType().equals("TRANSPORT_UNIT") &&
                                    transportUnitRepository.findById(Integer.parseInt(r.getBeneficiaryId().toString()))
                                            .map(tu -> tu.getNameCompany().equals(unit))
                                            .orElse(false)))
                    .collect(Collectors.toList());

            response.setShipmentGrowth(prevBookings.isEmpty() ? 0 : ((double) bookings.size() - prevBookings.size()) / prevBookings.size() * 100);
            response.setRevenueGrowth(prevRevenues.isEmpty() || prevRevenues.stream().map(Revenue::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue() == 0 ? 0 : revenues.stream()
                    .map(Revenue::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .subtract(prevRevenues.stream().map(Revenue::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add))
                    .divide(prevRevenues.stream().map(Revenue::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add), 2, BigDecimal.ROUND_HALF_UP)
                    .doubleValue() * 100);
            response.setDeliveryRateGrowth(prevBookings.isEmpty() ? 0 : response.getDeliveryRate() - ((double) prevBookings.stream()
                    .filter(b -> "COMPLETED".equals(b.getStatus()) && b.getTransports() != null && b.getTransports().stream().anyMatch(t -> "ON_TIME".equals(t.getStatus())))
                    .count() / prevBookings.size() * 100));
            response.setVolumeGrowth(prevBookings.isEmpty() || prevBookings.stream()
                    .flatMap(b -> b.getItems().stream())
                    .mapToDouble(item -> item.getWeight() != null ? item.getWeight().doubleValue() : 0)
                    .sum() / 1000 == 0 ? 0 : (response.getTotalVolume() - prevBookings.stream()
                    .flatMap(b -> b.getItems().stream())
                    .mapToDouble(item -> item.getWeight() != null ? item.getWeight().doubleValue() : 0)
                    .sum() / 1000) / (prevBookings.stream()
                    .flatMap(b -> b.getItems().stream())
                    .mapToDouble(item -> item.getWeight() != null ? item.getWeight().doubleValue() : 0)
                    .sum() / 1000) * 100);

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return new TransportDataResponse();
        }
    }

    @Override
    public List<RankingDataResponse> getRankingData(String period, String metric) {
        try {
            List<OperatorStaff> staffList = operatorStaffRepository.findAll();
            LocalDate startDate;
            LocalDate endDate = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));

            switch (period) {
                case "week":
                    startDate = endDate.minusWeeks(1);
                    break;
                case "month":
                    startDate = endDate.minusMonths(1);
                    break;
                case "quarter":
                    startDate = endDate.minusMonths(3);
                    break;
                case "year":
                    startDate = endDate.minusYears(1);
                    break;
                default:
                    startDate = LocalDate.of(2000, 1, 1);
            }

            List<RankingDataResponse> result = new ArrayList<>();
            for (OperatorStaff staff : staffList) {
                List<Booking> bookings = bookingRepository.findByOperatorStaff(staff).stream()
                        .filter(b -> b.getDeliveryDate() != null && !b.getDeliveryDate().toLocalDate().isBefore(startDate) && !b.getDeliveryDate().toLocalDate().isAfter(endDate))
                        .collect(Collectors.toList());
                List<Revenue> revenues = revenueRepository.findAll().stream()
                        .filter(r -> r.getBeneficiaryType().equals("OPERATOR_STAFF") &&
                                r.getBeneficiaryId().equals(staff.getOperatorId()) &&
                                !r.getDate().isBefore(startDate) && !r.getDate().isAfter(endDate))
                        .collect(Collectors.toList());
                List<Feedback> feedbacks = feedbackRepository.findByOperatorStaff(staff).stream()
                        .filter(f -> !f.getCreatedAt().toLocalDate().isBefore(startDate) && !f.getCreatedAt().toLocalDate().isAfter(endDate))
                        .collect(Collectors.toList());

                RankingDataResponse response = new RankingDataResponse();
                response.setName(staff.getUsers() != null ? staff.getUsers().getFullName() : "Unknown");
                response.setUnit(bookings.isEmpty() ? "Unknown Unit" : bookings.get(0).getTransportUnit().getNameCompany());
                response.setRevenue(DECIMAL_FORMAT.format(revenues.stream()
                        .map(r -> r.getAmount() != null ? r.getAmount() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)));
                response.setTrips(bookings.size());
                response.setSuccessRate(bookings.isEmpty() ? 0 : (double) bookings.stream()
                        .filter(b -> "COMPLETED".equals(b.getStatus()) && b.getTransports() != null && b.getTransports().stream().anyMatch(t -> "ON_TIME".equals(t.getStatus())))
                        .count() / bookings.size() * 100);

                String fullName = staff.getUsers() != null ? staff.getUsers().getFullName() : "";
                String avatar = "";
                if (fullName != null && !fullName.trim().isEmpty()) {
                    avatar = Arrays.stream(fullName.split(" "))
                            .filter(w -> !w.isEmpty())
                            .map(w -> w.substring(0, 1))
                            .collect(Collectors.joining())
                            .toUpperCase();
                    avatar = avatar.substring(0, Math.min(2, avatar.length()));
                }
                response.setAvatar(avatar);

                LocalDate prevStartDate = startDate.minusDays(java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1);
                LocalDate prevEndDate = startDate.minusDays(1);

                List<Booking> prevBookings = bookingRepository.findByOperatorStaff(staff).stream()
                        .filter(b -> b.getDeliveryDate() != null && !b.getDeliveryDate().toLocalDate().isBefore(prevStartDate) && !b.getDeliveryDate().toLocalDate().isAfter(prevEndDate))
                        .collect(Collectors.toList());
                List<Revenue> prevRevenues = revenueRepository.findAll().stream()
                        .filter(r -> r.getBeneficiaryType().equals("OPERATOR_STAFF") &&
                                r.getBeneficiaryId().equals(staff.getOperatorId()) &&
                                !r.getDate().isBefore(prevStartDate) && !r.getDate().isAfter(prevEndDate))
                        .collect(Collectors.toList());
                List<Feedback> prevFeedbacks = feedbackRepository.findByOperatorStaff(staff).stream()
                        .filter(f -> !f.getCreatedAt().toLocalDate().isBefore(prevStartDate) && !f.getCreatedAt().toLocalDate().isAfter(prevEndDate))
                        .collect(Collectors.toList());

                double prevValue = 0;
                switch (metric) {
                    case "revenue":
                        prevValue = prevRevenues.stream()
                                .map(r -> r.getAmount() != null ? r.getAmount() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue();
                        break;
                    case "trips":
                        prevValue = prevBookings.size();
                        break;
                    case "success_rate":
                        prevValue = prevBookings.isEmpty() ? 0 : (double) prevBookings.stream()
                                .filter(b -> "COMPLETED".equals(b.getStatus()) && b.getTransports() != null && b.getTransports().stream().anyMatch(t -> "ON_TIME".equals(t.getStatus())))
                                .count() / prevBookings.size() * 100;
                        break;
                    case "customer_rating":
                        prevValue = prevFeedbacks.isEmpty() ? 0 : prevFeedbacks.stream().mapToDouble(Feedback::getStar).average().orElse(0);
                        break;
                }

                double currentValue = 0;
                switch (metric) {
                    case "revenue":
                        currentValue = revenues.stream()
                                .map(r -> r.getAmount() != null ? r.getAmount() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue();
                        break;
                    case "trips":
                        currentValue = bookings.size();
                        break;
                    case "success_rate":
                        currentValue = response.getSuccessRate();
                        break;
                    case "customer_rating":
                        currentValue = feedbacks.isEmpty() ? 0 : feedbacks.stream().mapToDouble(Feedback::getStar).average().orElse(0);
                        break;
                }

                double change = prevValue == 0 ? 0 : (currentValue - prevValue) / prevValue * 100;
                response.setChange(String.format("%.1f%%", Math.abs(change)));
                response.setTrend(change >= 0 ? "up" : "down");

                result.add(response);
            }

            result.sort((a, b) -> {
                double ratingA = 0;
                double ratingB = 0;
                if (metric.equals("customer_rating")) {
                    Optional<OperatorStaff> staffA = staffList.stream()
                            .filter(s -> s.getUsers() != null && a.getName().equals(s.getUsers().getFullName()))
                            .findFirst();
                    if (staffA.isPresent()) {
                        ratingA = feedbackRepository.findByOperatorStaff(staffA.get()).stream()
                                .filter(f -> !f.getCreatedAt().toLocalDate().isBefore(startDate) && !f.getCreatedAt().toLocalDate().isAfter(endDate))
                                .mapToDouble(Feedback::getStar)
                                .average().orElse(0);
                    }

                    Optional<OperatorStaff> staffB = staffList.stream()
                            .filter(s -> s.getUsers() != null && b.getName().equals(s.getUsers().getFullName()))
                            .findFirst();
                    if (staffB.isPresent()) {
                        ratingB = feedbackRepository.findByOperatorStaff(staffB.get()).stream()
                                .filter(f -> !f.getCreatedAt().toLocalDate().isBefore(startDate) && !f.getCreatedAt().toLocalDate().isAfter(endDate))
                                .mapToDouble(Feedback::getStar)
                                .average().orElse(0);
                    }
                }

                return switch (metric) {
                    case "revenue" -> BigDecimal.valueOf(Double.parseDouble(b.getRevenue().replace(" đ", "").replace(",", "")))
                            .compareTo(BigDecimal.valueOf(Double.parseDouble(a.getRevenue().replace(" đ", "").replace(",", ""))));
                    case "trips" -> Integer.compare(b.getTrips(), a.getTrips());
                    case "success_rate" -> Double.compare(b.getSuccessRate(), a.getSuccessRate());
                    case "customer_rating" -> Double.compare(ratingB, ratingA);
                    default -> 0;
                };
            });

            for (int i = 0; i < result.size(); i++) {
                result.get(i).setRank(i + 1);
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<TeamRankingResponse> getTeamRanking(String period, String metric) {
        try {
            List<TransportUnit> units = transportUnitRepository.findAll();
            LocalDate startDate;
            LocalDate endDate = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));

            switch (period) {
                case "week":
                    startDate = endDate.minusWeeks(1);
                    break;
                case "month":
                    startDate = endDate.minusMonths(1);
                    break;
                case "quarter":
                    startDate = endDate.minusMonths(3);
                    break;
                case "year":
                    startDate = endDate.minusYears(1);
                    break;
                default:
                    startDate = LocalDate.of(2000, 1, 1);
            }

            List<TeamRankingResponse> result = new ArrayList<>();
            for (TransportUnit unit : units) {
                List<Booking> bookings = bookingRepository.findByTransportUnit(unit).stream()
                        .filter(b -> b.getDeliveryDate() != null && !b.getDeliveryDate().toLocalDate().isBefore(startDate) && !b.getDeliveryDate().toLocalDate().isAfter(endDate))
                        .collect(Collectors.toList());
                List<Revenue> revenues = revenueRepository.findAll().stream()
                        .filter(r -> r.getBeneficiaryType().equals("TRANSPORT_UNIT") &&
                                r.getBeneficiaryId().equals(unit.getTransportId()) &&
                                !r.getDate().isBefore(startDate) && !r.getDate().isAfter(endDate))
                        .collect(Collectors.toList());
                List<OperatorStaff> staffList = bookings.stream()
                        .map(Booking::getOperatorStaff)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList());

                TeamRankingResponse response = new TeamRankingResponse();
                response.setName(unit.getNameCompany());
                response.setTotalRevenue(DECIMAL_FORMAT.format(revenues.stream()
                        .map(r -> r.getAmount() != null ? r.getAmount() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)));
                response.setTotalTrips(bookings.size());
                response.setAvgSuccessRate(bookings.isEmpty() ? 0 : (double) bookings.stream()
                        .filter(b -> "COMPLETED".equals(b.getStatus()) && b.getTransports() != null && b.getTransports().stream().anyMatch(t -> "ON_TIME".equals(t.getStatus())))
                        .count() / bookings.size() * 100);
                response.setMembers(staffList.size());

                LocalDate prevStartDate = startDate.minusDays(java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1);
                LocalDate prevEndDate = startDate.minusDays(1);

                List<Booking> prevBookings = bookingRepository.findByTransportUnit(unit).stream()
                        .filter(b -> b.getDeliveryDate() != null && !b.getDeliveryDate().toLocalDate().isBefore(prevStartDate) && !b.getDeliveryDate().toLocalDate().isAfter(prevEndDate))
                        .collect(Collectors.toList());
                List<Revenue> prevRevenues = revenueRepository.findAll().stream()
                        .filter(r -> r.getBeneficiaryType().equals("TRANSPORT_UNIT") &&
                                r.getBeneficiaryId().equals(unit.getTransportId()) &&
                                !r.getDate().isBefore(prevStartDate) && !r.getDate().isAfter(prevEndDate))
                        .collect(Collectors.toList());
                List<Feedback> prevFeedbacks = staffList.stream()
                        .flatMap(staff -> feedbackRepository.findByOperatorStaff(staff).stream())
                        .filter(f -> !f.getCreatedAt().toLocalDate().isBefore(prevStartDate) && !f.getCreatedAt().toLocalDate().isAfter(prevEndDate))
                        .collect(Collectors.toList());

                double prevValue = 0;
                switch (metric) {
                    case "revenue":
                        prevValue = prevRevenues.stream().map(Revenue::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue();
                        break;
                    case "trips":
                        prevValue = prevBookings.size();
                        break;
                    case "success_rate":
                        prevValue = prevBookings.isEmpty() ? 0 : (double) prevBookings.stream()
                                .filter(b -> "COMPLETED".equals(b.getStatus()) && b.getTransports() != null && b.getTransports().stream().anyMatch(t -> "ON_TIME".equals(t.getStatus())))
                                .count() / prevBookings.size() * 100;
                        break;
                    case "customer_rating":
                        prevValue = prevFeedbacks.isEmpty() ? 0 : prevFeedbacks.stream().mapToDouble(Feedback::getStar).average().orElse(0);
                        break;
                }
                double currentValue = 0;
                switch (metric) {
                    case "revenue":
                        currentValue = revenues.stream().map(Revenue::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue();
                        break;
                    case "trips":
                        currentValue = bookings.size();
                        break;
                    case "success_rate":
                        currentValue = response.getAvgSuccessRate();
                        break;
                    case "customer_rating":
                        currentValue = staffList.isEmpty() ? 0 : staffList.stream()
                                .mapToDouble(s -> feedbackRepository.findByOperatorStaff(s).stream()
                                        .filter(f -> !f.getCreatedAt().toLocalDate().isBefore(startDate) && !f.getCreatedAt().toLocalDate().isAfter(endDate))
                                        .mapToDouble(Feedback::getStar)
                                        .average().orElse(0))
                                .average().orElse(0);
                        break;
                }
                double change = prevValue == 0 ? 0 : (currentValue - prevValue) / prevValue * 100;
                response.setChange(String.format("%.1f%%", Math.abs(change)));
                response.setTrend(change >= 0 ? "up" : "down");

                result.add(response);
            }

            result.sort((a, b) -> {
                double ratingA = 0;
                double ratingB = 0;
                if (metric.equals("customer_rating")) {
                    Optional<TransportUnit> unitA = transportUnitRepository.findAll().stream()
                            .filter(t -> t.getNameCompany().equals(a.getName()))
                            .findFirst();
                    if (unitA.isPresent()) {
                        List<Booking> bookingsA = bookingRepository.findByTransportUnit(unitA.get());
                        ratingA = bookingsA.stream()
                                .map(Booking::getOperatorStaff)
                                .filter(Objects::nonNull)
                                .distinct()
                                .mapToDouble(s -> feedbackRepository.findByOperatorStaff(s).stream()
                                        .filter(f -> !f.getCreatedAt().toLocalDate().isBefore(startDate) && !f.getCreatedAt().toLocalDate().isAfter(endDate))
                                        .mapToDouble(Feedback::getStar)
                                        .average().orElse(0))
                                .average().orElse(0);
                    }

                    Optional<TransportUnit> unitB = transportUnitRepository.findAll().stream()
                            .filter(t -> t.getNameCompany().equals(b.getName()))
                            .findFirst();
                    if (unitB.isPresent()) {
                        List<Booking> bookingsB = bookingRepository.findByTransportUnit(unitB.get());
                        ratingB = bookingsB.stream()
                                .map(Booking::getOperatorStaff)
                                .filter(Objects::nonNull)
                                .distinct()
                                .mapToDouble(s -> feedbackRepository.findByOperatorStaff(s).stream()
                                        .filter(f -> !f.getCreatedAt().toLocalDate().isBefore(startDate) && !f.getCreatedAt().toLocalDate().isAfter(endDate))
                                        .mapToDouble(Feedback::getStar)
                                        .average().orElse(0))
                                .average().orElse(0);
                    }
                }

                return switch (metric) {
                    case "revenue" -> BigDecimal.valueOf(Double.parseDouble(b.getTotalRevenue().replace(" đ", "").replace(",", "")))
                            .compareTo(BigDecimal.valueOf(Double.parseDouble(a.getTotalRevenue().replace(" đ", "").replace(",", ""))));
                    case "trips" -> Integer.compare(b.getTotalTrips(), a.getTotalTrips());
                    case "success_rate" -> Double.compare(b.getAvgSuccessRate(), a.getAvgSuccessRate());
                    case "customer_rating" -> Double.compare(ratingB, ratingA);
                    default -> 0;
                };
            });

            for (int i = 0; i < result.size(); i++) {
                result.get(i).setRank(i + 1);
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<AchievementResponse> getAchievements() {
        try {
            LocalDate startDate = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).minusMonths(1);
            LocalDate endDate = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));

            List<OperatorStaff> staffList = operatorStaffRepository.findAll();
            List<TransportUnit> units = transportUnitRepository.findAll();

            OperatorStaff topTripsStaff = staffList.stream()
                    .max(Comparator.comparing(s -> bookingRepository.findByOperatorStaff(s).stream()
                            .filter(b -> b.getDeliveryDate() != null && !b.getDeliveryDate().toLocalDate().isBefore(startDate) && !b.getDeliveryDate().toLocalDate().isAfter(endDate))
                            .filter(b -> "COMPLETED".equals(b.getStatus()))
                            .count()))
                    .orElse(null);

            OperatorStaff topSuccessRateStaff = staffList.stream()
                    .max(Comparator.comparing(s -> {
                        List<Booking> bookings = bookingRepository.findByOperatorStaff(s).stream()
                                .filter(b -> b.getDeliveryDate() != null && !b.getDeliveryDate().toLocalDate().isBefore(startDate) && !b.getDeliveryDate().toLocalDate().isAfter(endDate))
                                .collect(Collectors.toList());
                        return bookings.isEmpty() ? 0 : (double) bookings.stream()
                                .filter(b -> "COMPLETED".equals(b.getStatus()) && b.getTransports() != null && b.getTransports().stream().anyMatch(t -> "ON_TIME".equals(t.getStatus())) && !b.getDeliveryDate().toLocalDate().isAfter(LocalDate.now()))
                                .count() / bookings.size() * 100;
                    }))
                    .orElse(null);

            TransportUnit topGrowthUnit = units.stream()
                    .max(Comparator.comparing(u -> {
                        List<Revenue> revenues = revenueRepository.findAll().stream()
                                .filter(r -> r.getBeneficiaryType().equals("TRANSPORT_UNIT") &&
                                        r.getBeneficiaryId().equals(u.getTransportId()) &&
                                        !r.getDate().isBefore(startDate) && !r.getDate().isAfter(endDate))
                                .collect(Collectors.toList());
                        List<Revenue> prevRevenues = revenueRepository.findAll().stream()
                                .filter(r -> r.getBeneficiaryType().equals("TRANSPORT_UNIT") &&
                                        r.getBeneficiaryId().equals(u.getTransportId()) &&
                                        !r.getDate().isBefore(startDate.minusMonths(1)) && !r.getDate().isAfter(startDate.minusDays(1)))
                                .collect(Collectors.toList());
                        double currentRevenue = revenues.stream().map(Revenue::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue();
                        double prevRevenue = prevRevenues.stream().map(Revenue::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue();
                        return prevRevenue == 0 ? 0 : (currentRevenue - prevRevenue) / prevRevenue * 100;
                    }))
                    .orElse(null);

            List<AchievementResponse> result = new ArrayList<>();
            if (topTripsStaff != null) {
                long trips = bookingRepository.findByOperatorStaff(topTripsStaff).stream()
                        .filter(b -> b.getDeliveryDate() != null && !b.getDeliveryDate().toLocalDate().isBefore(startDate) && !b.getDeliveryDate().toLocalDate().isAfter(endDate))
                        .filter(b -> "COMPLETED".equals(b.getStatus()))
                        .count();
                AchievementResponse tripsAchievement = new AchievementResponse();
                tripsAchievement.setValue(String.valueOf(trips));
                tripsAchievement.setLabel("Chuyến giao thành công cao nhất");
                tripsAchievement.setName(topTripsStaff.getUsers().getFullName());
                result.add(tripsAchievement);
            }

            if (topSuccessRateStaff != null) {
                List<Booking> bookings = bookingRepository.findByOperatorStaff(topSuccessRateStaff).stream()
                        .filter(b -> b.getDeliveryDate() != null && !b.getDeliveryDate().toLocalDate().isBefore(startDate) && !b.getDeliveryDate().toLocalDate().isAfter(endDate))
                        .collect(Collectors.toList());
                double successRate = bookings.isEmpty() ? 0 : (double) bookings.stream()
                        .filter(b -> "COMPLETED".equals(b.getStatus()) && b.getTransports() != null && b.getTransports().stream().anyMatch(t -> "ON_TIME".equals(t.getStatus())) && b.getDeliveryDate() != null && !b.getDeliveryDate().toLocalDate().isAfter(LocalDate.now()))
                        .count() / bookings.size() * 100;
                AchievementResponse successRateAchievement = new AchievementResponse();
                successRateAchievement.setValue(String.format("%.1f%%", successRate));
                successRateAchievement.setLabel("Tỷ lệ thành công cao nhất");
                successRateAchievement.setName(topSuccessRateStaff.getUsers().getFullName());
                result.add(successRateAchievement);
            }

            if (topGrowthUnit != null) {
                List<Revenue> revenues = revenueRepository.findAll().stream()
                        .filter(r -> r.getBeneficiaryType().equals("TRANSPORT_UNIT") &&
                                r.getBeneficiaryId().equals(topGrowthUnit.getTransportId()) &&
                                !r.getDate().isBefore(startDate) && !r.getDate().isAfter(endDate))
                        .collect(Collectors.toList());
                List<Revenue> prevRevenues = revenueRepository.findAll().stream()
                        .filter(r -> r.getBeneficiaryType().equals("TRANSPORT_UNIT") &&
                                r.getBeneficiaryId().equals(topGrowthUnit.getTransportId()) &&
                                !r.getDate().isBefore(startDate.minusMonths(1)) && !r.getDate().isAfter(startDate.minusDays(1)))
                        .collect(Collectors.toList());
                double currentRevenue = revenues.stream().map(Revenue::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue();
                double prevRevenue = prevRevenues.stream().map(Revenue::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue();
                double growth = prevRevenue == 0 ? 0 : (currentRevenue - prevRevenue) / prevRevenue * 100;
                AchievementResponse growthAchievement = new AchievementResponse();
                growthAchievement.setValue(String.format("+%.1f%%", growth));
                growthAchievement.setLabel("Tăng trưởng cao nhất");
                growthAchievement.setName(topGrowthUnit.getNameCompany());
                result.add(growthAchievement);
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}