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
import org.example.operatormanagementsystem.entity.*;
import org.example.operatormanagementsystem.managecustomerorderbystaff.repository.BookingRepository;
import org.example.operatormanagementsystem.managecustomerorderbystaff.repository.CustomerRepository;
import org.example.operatormanagementsystem.repository.UserRepository;
import org.example.operatormanagementsystem.repository.RevenueRepository;
import org.example.operatormanagementsystem.transportunit.repository.TransportUnitRepository;
import org.example.operatormanagementsystem.managestaff_yen.repository.OperatorStaffRepository;
import org.example.operatormanagementsystem.dashboardstaff.repository.FeedbackRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.OptionalInt;

@Service
@RequiredArgsConstructor
public class DashboardStaffServiceImpl implements DashboardStaffService {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,### đ");

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final RevenueRepository revenueRepository;
    private final TransportUnitRepository transportUnitRepository;
    private final OperatorStaffRepository operatorStaffRepository;
    private final FeedbackRepository feedbackRepository;

    @PersistenceContext
    private EntityManager entityManager;



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
    public List<MonthlyRevenueResponse> getMonthlyRevenue(String year, String unit, String startMonth, String endMonth) {
        try {

            int startMonthInt = 1;
            int endMonthInt = 12;
            try {
                if (startMonth != null && !startMonth.equals("")) startMonthInt = Integer.parseInt(startMonth);
                if (endMonth != null && !endMonth.equals("")) endMonthInt = Integer.parseInt(endMonth);
            } catch (Exception e) {
                startMonthInt = 1;
                endMonthInt = 12;
            }

            List<Revenue> revenues = revenueRepository.findAll();
            LocalDate startDate = year.equals("Tất cả") ? LocalDate.of(2000, startMonthInt, 1) : LocalDate.of(Integer.parseInt(year), startMonthInt, 1);
            LocalDate endDate = year.equals("Tất cả") ? LocalDate.now() : LocalDate.of(Integer.parseInt(year), endMonthInt, java.time.YearMonth.of(Integer.parseInt(year), endMonthInt).lengthOfMonth());

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
                response.setDvChuyenNhaSaiGon(unitMap.getOrDefault("DV Chuyển Nhà SG", BigDecimal.ZERO));
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
    public List<PerformanceDataResponse> getPerformanceData(String year, String unit, String startMonth, String endMonth) {
        try {

            int startMonthInt = 1;
            int endMonthInt = 12;
            try {
                if (startMonth != null && !startMonth.equals("")) startMonthInt = Integer.parseInt(startMonth);
                if (endMonth != null && !endMonth.equals("")) endMonthInt = Integer.parseInt(endMonth);
            } catch (Exception e) {
                startMonthInt = 1;
                endMonthInt = 12;
            }

            List<Booking> bookings = bookingRepository.findAll();
            LocalDate startDate = year.equals("Tất cả") ? LocalDate.of(2000, startMonthInt, 1) : LocalDate.of(Integer.parseInt(year), startMonthInt, 1);
            LocalDate endDate = year.equals("Tất cả") ? LocalDate.now() : LocalDate.of(Integer.parseInt(year), endMonthInt, java.time.YearMonth.of(Integer.parseInt(year), endMonthInt).lengthOfMonth());

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
                // Thêm log debug
                System.out.println("=== DEBUG: Tháng " + month + " ===");
                bookingList.forEach(b -> System.out.println("BookingId: " + b.getBookingId() + ", Status: " + b.getStatus()));
                long huyCount = bookingList.stream().filter(b -> "CANCELED".equals(b.getStatus())).count();
                System.out.println("Số booking huỷ (CANCELED) tháng " + month + ": " + huyCount);

                PerformanceDataResponse response = new PerformanceDataResponse();
                response.setMonth(month);
                response.setDungHan((int) bookingList.stream()
                        .filter(b -> "COMPLETED".equals(b.getStatus()) && b.getDeliveryDate() != null && !b.getDeliveryDate().toLocalDate().isAfter(LocalDate.now()))
                        .count());
                response.setHuy((int) huyCount);
                response.setTre((int) bookingList.stream()
                        .filter(b -> "COMPLETED".equals(b.getStatus())
                                && b.getDeliveryDate() != null
                                && b.getCreatedAt() != null
                                && b.getCreatedAt().toLocalDate().isAfter(b.getDeliveryDate().toLocalDate()))
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
    public List<DetailDataResponse> getDetailData(String year, String unit, String startMonth, String endMonth) {
        try {
            List<Booking> bookings = bookingRepository.findAll();

            int startMonthInt = 1;
            int endMonthInt = 12;
            try {
                if (startMonth != null && !startMonth.equals("")) startMonthInt = Integer.parseInt(startMonth);
                if (endMonth != null && !endMonth.equals("")) endMonthInt = Integer.parseInt(endMonth);
            } catch (Exception e) {
                startMonthInt = 1;
                endMonthInt = 12;
            }

            LocalDate startDate = year.equals("Tất cả") ? LocalDate.of(2000, startMonthInt, 1) : LocalDate.of(Integer.parseInt(year), startMonthInt, 1);
            LocalDate endDate = year.equals("Tất cả") ? LocalDate.now() : LocalDate.of(Integer.parseInt(year), endMonthInt, java.time.YearMonth.of(Integer.parseInt(year), endMonthInt).lengthOfMonth());

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
                    int onTime = 0, cancelled = 0, late = 0, trips = 0;
                    for (Booking b : bookingList) {
                        if ("CANCELED".equals(b.getStatus())) {
                            cancelled++;
                            trips++;
                        } else if ("COMPLETED".equals(b.getStatus()) && b.getDeliveryDate() != null && b.getCreatedAt() != null) {
                            if (b.getCreatedAt().toLocalDate().isAfter(b.getDeliveryDate().toLocalDate())) {
                                late++;
                            } else {
                                onTime++;
                            }
                            trips++;
                        }
                    }
                    response.setTrips(trips);
                    response.setOnTime(onTime);
                    response.setCancelled(cancelled);
                    response.setLate(late);
                    response.setRevenue(DECIMAL_FORMAT.format(bookingList.stream()
                            .map(b -> new BigDecimal(b.getTotal()))
                            .reduce(BigDecimal.ZERO, BigDecimal::add)));
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
    public TransportDataResponse getTransportData(String year, String unit, String startMonth, String endMonth) {
        try {
            List<Booking> allBookings = bookingRepository.findAll();
            List<Revenue> allRevenues = revenueRepository.findAll();

            int startMonthInt = 1;
            int endMonthInt = 12;
            try {
                if (startMonth != null && !startMonth.equals("")) startMonthInt = Integer.parseInt(startMonth);
                if (endMonth != null && !endMonth.equals("")) endMonthInt = Integer.parseInt(endMonth);
            } catch (Exception e) {
                startMonthInt = 1;
                endMonthInt = 12;
            }

            String yearParam = year != null ? year.trim() : "";
            boolean calculateGrowth = false;
            int selectedYear = -1;

            if (yearParam.equalsIgnoreCase("Tất cả") || yearParam.isEmpty()) {
                OptionalInt maxYear = allBookings.stream()
                        .mapToInt(b -> {
                            if (b.getDeliveryDate() != null) return b.getDeliveryDate().toLocalDate().getYear();
                            if (b.getCreatedAt() != null) return b.getCreatedAt().toLocalDate().getYear();
                            return 0;
                        })
                        .max();
                if (maxYear.isPresent() && maxYear.getAsInt() > 0) {
                    selectedYear = maxYear.getAsInt();
                    calculateGrowth = true;
                    System.out.println("==DEBUG== year param is 'Tất cả', using max year in DB: " + selectedYear);
                } else {
                    calculateGrowth = false;
                }
            } else if (yearParam.matches("\\d{4}")) {
                selectedYear = Integer.parseInt(yearParam);
                calculateGrowth = true;
            } else {
                System.out.println("==DEBUG== year param is not a valid number, growth will be 0");
                calculateGrowth = false;
            }
            System.out.println("==DEBUG== year param (final): [" + yearParam + "]");


            LocalDate finalStartDate;
            LocalDate finalEndDate;
            LocalDate finalPrevStartDate;
            LocalDate finalPrevEndDate;

            if (calculateGrowth) {
                finalStartDate = LocalDate.of(selectedYear, startMonthInt, 1);
                finalEndDate = LocalDate.of(selectedYear, endMonthInt, java.time.YearMonth.of(selectedYear, endMonthInt).lengthOfMonth());
                finalPrevStartDate = finalStartDate.minusYears(1);
                finalPrevEndDate = finalEndDate.minusYears(1);
            } else {
                finalStartDate = null;
                finalEndDate = null;
                finalPrevStartDate = null;
                finalPrevEndDate = null;
            }

            final boolean isGrowth = calculateGrowth;
            List<Booking> bookings;
            List<Revenue> revenues;

            if (yearParam.equalsIgnoreCase("Tất cả") || yearParam.isEmpty()) {
                final int finalSelectedYear = selectedYear;
                bookings = allBookings.stream()
                        .filter(b -> {
                            LocalDate bookingDate = b.getDeliveryDate() != null ? b.getDeliveryDate().toLocalDate() : null;
                            return bookingDate != null && bookingDate.getYear() == finalSelectedYear;
                        })
                        .filter(b -> unit.equals("Tất cả") ||
                                (b.getTransportUnit() != null && b.getTransportUnit().getNameCompany().equals(unit)))
                        .collect(Collectors.toList());

                revenues = allRevenues.stream()
                        .filter(r -> r.getDate() != null && r.getDate().getYear() == finalSelectedYear)
                        .filter(r -> unit.equals("Tất cả") ||
                                (r.getBeneficiaryType().equals("TRANSPORT_UNIT") &&
                                        transportUnitRepository.findById(Integer.parseInt(r.getBeneficiaryId().toString()))
                                                .map(tu -> tu.getNameCompany().equals(unit))
                                                .orElse(false)))
                        .collect(Collectors.toList());
            } else {
                bookings = allBookings.stream()
                        .filter(b -> {
                            LocalDate bookingDate = b.getDeliveryDate() != null ? b.getDeliveryDate().toLocalDate() : null;
                            return bookingDate != null && !bookingDate.isBefore(finalStartDate) && !bookingDate.isAfter(finalEndDate);
                        })
                        .filter(b -> unit.equals("Tất cả") ||
                                (b.getTransportUnit() != null && b.getTransportUnit().getNameCompany().equals(unit)))
                        .collect(Collectors.toList());

                revenues = allRevenues.stream()
                        .filter(r -> !r.getDate().isBefore(finalStartDate) && !r.getDate().isAfter(finalEndDate))
                        .filter(r -> unit.equals("Tất cả") ||
                                (r.getBeneficiaryType().equals("TRANSPORT_UNIT") &&
                                        transportUnitRepository.findById(Integer.parseInt(r.getBeneficiaryId().toString()))
                                                .map(tu -> tu.getNameCompany().equals(unit))
                                                .orElse(false)))
                        .collect(Collectors.toList());
            }

            List<Booking> prevBookings = allBookings.stream()
                    .filter(b -> {
                        LocalDate bookingDate = b.getDeliveryDate() != null ? b.getDeliveryDate().toLocalDate() : null;
                        return isGrowth && bookingDate != null && !bookingDate.isBefore(finalPrevStartDate) && !bookingDate.isAfter(finalPrevEndDate);
                    })
                    .filter(b -> unit.equals("Tất cả") ||
                            (b.getTransportUnit() != null && b.getTransportUnit().getNameCompany().equals(unit)))
                    .collect(Collectors.toList());

            List<Revenue> prevRevenues = allRevenues.stream()
                    .filter(r -> isGrowth && !r.getDate().isBefore(finalPrevStartDate) && !r.getDate().isAfter(finalPrevEndDate))
                    .filter(r -> unit.equals("Tất cả") ||
                            (r.getBeneficiaryType().equals("TRANSPORT_UNIT") &&
                                    transportUnitRepository.findById(Integer.parseInt(r.getBeneficiaryId().toString()))
                                            .map(tu -> tu.getNameCompany().equals(unit))
                                            .orElse(false)))
                    .collect(Collectors.toList());

            System.out.println("Current year: " + selectedYear + ", Bookings: " + bookings.size() + ", Revenue: " + revenues.size());
            System.out.println("Prev year: " + (selectedYear-1) + ", Bookings: " + prevBookings.size() + ", Revenue: " + prevRevenues.size());

            TransportDataResponse response = new TransportDataResponse();

            response.setTotalShipments(bookings.size());

            BigDecimal totalRevenue = revenues.stream()
                    .map(r -> r.getAmount() != null ? r.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            response.setRevenue(DECIMAL_FORMAT.format(totalRevenue));

            long completedTransportBookings = bookings.stream()
                    .filter(b -> "COMPLETED".equals(b.getStatus()))
                    .count();

            double currentDeliveryRate = bookings.isEmpty() ? 0 :
                    (double) completedTransportBookings / bookings.size() * 100;
            currentDeliveryRate = Math.round(currentDeliveryRate * 100.0) / 100.0;
            response.setDeliveryRate(currentDeliveryRate);

            double totalWeight = 0.0;
            for (Booking booking : bookings) {
                try {
                    if (booking.getItems() != null) {
                        for (Items item : booking.getItems()) {
                            Double itemWeight = item.getWeight();
                            if (itemWeight != null) {
                                totalWeight += itemWeight;
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error loading items for booking " + booking.getBookingId());
                }
            }
            double currentVolume = totalWeight / 1000;
            response.setTotalVolume(currentVolume);

            BigDecimal prevTotalRevenue = prevRevenues.stream()
                    .map(r -> r.getAmount() != null ? r.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long prevCompletedTransportBookings = prevBookings.stream()
                    .filter(b -> "COMPLETED".equals(b.getStatus()))
                    .count();

            double prevDeliveryRate = prevBookings.isEmpty() ? 0 :
                    (double) prevCompletedTransportBookings / prevBookings.size() * 100;

            double prevTotalWeight = 0.0;
            for (Booking booking : prevBookings) {
                try {
                    if (booking.getItems() != null) {
                        for (Items item : booking.getItems()) {
                            Double itemWeight = item.getWeight();
                            if (itemWeight != null) {
                                prevTotalWeight += itemWeight;
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error loading items for prev booking " + booking.getBookingId());
                }
            }
            double prevVolume = prevTotalWeight / 1000;

            if (calculateGrowth) {
                double shipmentGrowth = prevBookings.isEmpty() ? 0 :
                        ((double) bookings.size() - prevBookings.size()) / prevBookings.size() * 100;
                shipmentGrowth = Math.round(shipmentGrowth * 100.0) / 100.0;
                response.setShipmentGrowth(shipmentGrowth);

                response.setRevenueGrowth(prevTotalRevenue.compareTo(BigDecimal.ZERO) == 0 ? 0 :
                        totalRevenue.subtract(prevTotalRevenue)
                                .divide(prevTotalRevenue, 4, BigDecimal.ROUND_HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                                .doubleValue());

                System.out.println("==DEBUG== deliveryRate this year: " + currentDeliveryRate + ", prev year: " + prevDeliveryRate);
                System.out.println("==DEBUG== volume this year: " + currentVolume + ", prev year: " + prevVolume);

                double deliveryRateGrowth = prevDeliveryRate == 0 ? 0 :
                        (currentDeliveryRate - prevDeliveryRate) / prevDeliveryRate * 100;
                deliveryRateGrowth = Math.round(deliveryRateGrowth * 100.0) / 100.0;
                response.setDeliveryRateGrowth(deliveryRateGrowth);

                double volumeGrowth = prevVolume == 0 ? 0 : (currentVolume - prevVolume) / prevVolume * 100;
                volumeGrowth = Math.round(volumeGrowth * 100.0) / 100.0;
                response.setVolumeGrowth(volumeGrowth);
            } else {
                response.setShipmentGrowth(0);
                response.setRevenueGrowth(0);
                response.setDeliveryRateGrowth(0);
                response.setVolumeGrowth(0);
            }

            System.out.println("=== GROWTH RESULTS ===");
            System.out.println("Shipment Growth: " + response.getShipmentGrowth() + "%");
            System.out.println("Revenue Growth: " + response.getRevenueGrowth() + "%");
            System.out.println("Delivery Rate Growth: " + response.getDeliveryRateGrowth() + "%");
            System.out.println("Volume Growth: " + response.getVolumeGrowth() + "%");

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
            System.out.println("=== StaffList size: " + staffList.size() + " ===");
            for (OperatorStaff s : staffList) {
                System.out.println("OperatorId: " + s.getOperatorId()
                        + ", UserId: " + (s.getUser() != null ? s.getUser().getId() : "null")
                        + ", UserName: " + (s.getUser() != null ? s.getUser().getFullName() : "null"));
            }
            List<Booking> allBookings = bookingRepository.findAll();
            List<Revenue> allRevenues = revenueRepository.findAll();
            List<TransportUnit> allUnits = transportUnitRepository.findAll();
            LocalDate startDate;
            LocalDate endDate = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));

            switch (period) {
                case "week":
                    startDate = endDate.minusWeeks(1);
                    break;
                case "month":
                    startDate = endDate.withDayOfMonth(1);
                    endDate = endDate.withDayOfMonth(endDate.lengthOfMonth());
                    break;
                case "quarter":
                    startDate = endDate.minusMonths(3);
                    break;
                case "year":
                    startDate = endDate.withDayOfYear(1);
                    endDate = endDate.withDayOfYear(endDate.lengthOfYear());
                    break;
                case "all":
                    startDate = LocalDate.of(2000, 1, 1);
                    endDate = LocalDate.of(2100, 12, 31);
                    break;
                default:
                    startDate = LocalDate.of(2020, 1, 1);
            }

            int currentMonth = endDate.getMonthValue();
            int currentYear = endDate.getYear();
            int prevMonth = currentMonth == 1 ? 12 : currentMonth - 1;
            int prevYear = currentMonth == 1 ? currentYear - 1 : currentYear;

            LocalDate prevStartDate, prevEndDate;
            switch (period) {
                case "week":
                    prevStartDate = startDate.minusWeeks(1);
                    prevEndDate = startDate.minusDays(1);
                    break;
                case "month":
                    prevStartDate = startDate.minusMonths(1);
                    prevEndDate = startDate.minusDays(1);
                    break;
                case "quarter":
                    prevStartDate = startDate.minusMonths(3);
                    prevEndDate = startDate.minusDays(1);
                    break;
                case "year":
                    prevStartDate = startDate.minusYears(1);
                    prevEndDate = startDate.minusDays(1);
                    break;
                default:
                    prevStartDate = LocalDate.of(2000, 1, 1);
                    prevEndDate = startDate.minusDays(1);
            }

            List<RankingDataResponse> result = new ArrayList<>();
            for (OperatorStaff staff : staffList) {
                List<Booking> bookings = allBookings.stream()
                        .filter(b -> b.getOperatorStaff() != null && b.getOperatorStaff().getOperatorId().equals(staff.getOperatorId()))
                        .filter(b -> b.getDeliveryDate() != null &&
                                b.getDeliveryDate().toLocalDate().getMonthValue() == currentMonth &&
                                b.getDeliveryDate().toLocalDate().getYear() == currentYear)
                        .collect(Collectors.toList());

                String unit = bookings.stream()
                        .filter(b -> b.getTransportUnit() != null)
                        .map(b -> {
                            Integer transportId = b.getTransportUnit().getTransportId();
                            return allUnits.stream()
                                    .filter(u -> u.getTransportId().equals(transportId))
                                    .map(TransportUnit::getNameCompany)
                                    .findFirst()
                                    .orElse(null);
                        })
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse("");

                BigDecimal revenue = allRevenues.stream()
                        .filter(r -> "OPERATOR_STAFF".equals(r.getBeneficiaryType()) &&
                                r.getBeneficiaryId() != null && staff.getOperatorId() != null &&
                                r.getBeneficiaryId().intValue() == staff.getOperatorId().intValue())
                        .filter(r -> r.getDate() != null &&
                                r.getDate().getMonthValue() == currentMonth &&
                                r.getDate().getYear() == currentYear)
                        .map(r -> r.getAmount() != null ? r.getAmount() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal prevRevenue = allRevenues.stream()
                        .filter(r -> "OPERATOR_STAFF".equals(r.getBeneficiaryType()) &&
                                r.getBeneficiaryId() != null && staff.getOperatorId() != null &&
                                r.getBeneficiaryId().intValue() == staff.getOperatorId().intValue())
                        .filter(r -> r.getDate() != null &&
                                r.getDate().getMonthValue() == prevMonth &&
                                r.getDate().getYear() == prevYear)
                        .map(r -> r.getAmount() != null ? r.getAmount() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                int trips = bookings.size();
                long completed = bookings.stream().filter(b -> "COMPLETED".equals(b.getStatus())).count();
                double successRate = trips == 0 ? 0 : (double) completed / trips * 100;

                final LocalDate filterStartDate = startDate;
                final LocalDate filterEndDate = endDate;
                List<Feedback> feedbacks = feedbackRepository.findByOperatorStaff(staff).stream()
                        .filter(f -> !f.getCreatedAt().toLocalDate().isBefore(filterStartDate) && !f.getCreatedAt().toLocalDate().isAfter(filterEndDate))
                        .collect(Collectors.toList());

                System.out.println("[RANKING-STAFF] Name: " + (staff.getUsers() != null ? staff.getUsers().getFullName() : "Unknown")
                        + ", OperatorId: " + staff.getOperatorId()
                        + ", Revenue: " + revenue
                        + ", PrevRevenue: " + prevRevenue
                        + ", Trips: " + trips
                        + ", Completed: " + completed
                        + ", SuccessRate: " + String.format("%.2f", successRate)
                        + ", Unit: " + unit
                        + ", Feedbacks: " + feedbacks.size());

                String formattedChange;
                String trend;
                double prevValue = prevRevenue.doubleValue();
                double currentValue = revenue.doubleValue();
                if (prevValue == 0 && currentValue > 0) {
                    formattedChange = "Mới";
                    trend = "up";
                } else if (prevValue == 0 && currentValue == 0) {
                    formattedChange = "0.0%";
                    trend = "up";
                } else {
                    double changePercentage = (currentValue - prevValue) / prevValue * 100;
                    formattedChange = String.format("%.1f%%", Math.abs(changePercentage));
                    trend = changePercentage >= 0 ? "up" : "down";
                }

                RankingDataResponse response = new RankingDataResponse();
                response.setName(staff.getUsers() != null ? staff.getUsers().getFullName() : "Unknown");
                response.setUnit(unit);
                response.setRevenue(DECIMAL_FORMAT.format(revenue));
                response.setTrips(trips);
                response.setSuccessRate(Math.round(successRate * 100.0) / 100.0);
                response.setAvatar(avatarFromName(response.getName()));
                response.setChange(formattedChange);
                response.setTrend(trend);
                result.add(response);
            }

            result = result.stream()
                    .filter(r -> r.getTrips() > 0 || !r.getRevenue().equals("0 đ"))
                    .collect(Collectors.toList());

            result.sort((a, b) -> {
                switch (metric) {
                    case "revenue":
                        return Double.compare(
                                Double.parseDouble(b.getRevenue().replace(" đ", "").replace(",", "")),
                                Double.parseDouble(a.getRevenue().replace(" đ", "").replace(",", ""))
                        );
                    case "trips":
                        return Integer.compare(b.getTrips(), a.getTrips());
                    case "success_rate":
                        return Double.compare(b.getSuccessRate(), a.getSuccessRate());
                    default:
                        return 0;
                }
            });

            List<RankingDataResponse> top10 = result.stream()
                    .limit(10)
                    .collect(Collectors.toList());
            for (int i = 0; i < top10.size(); i++) {
                top10.get(i).setRank(i + 1);
            }
            return top10;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private String avatarFromName(String name) {
        if (name == null || name.trim().isEmpty()) return "";
        String[] parts = name.trim().split(" ");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) sb.append(part.charAt(0));
            if (sb.length() == 2) break;
        }
        return sb.toString().toUpperCase();
    }

    @Override
    public List<TeamRankingResponse> getTeamRanking(String period, String metric) {
        try {
            List<TransportUnit> units = transportUnitRepository.findAll();
            LocalDate endDate = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            LocalDate startDate;

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
                case "all":
                    startDate = LocalDate.of(2000, 1, 1);
                    endDate = LocalDate.of(2100, 12, 31);
                    break;
                default:
                    startDate = LocalDate.of(2020, 1, 1);
            }

            System.out.println("[DEBUG] Query startDate: " + startDate + ", endDate: " + endDate);

            LocalDate extendedEndDate = LocalDate.of(2025, 12, 31);

            String sql = "SELECT beneficiary_id, SUM(amount) as total " +
                    "FROM revenue " +
                    "WHERE beneficiary_type = 'TRANSPORT_UNIT' " +
                    "AND date >= :startDate AND date <= :endDate " +
                    "GROUP BY beneficiary_id";
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            List<Object[]> revenueResults = query.getResultList();
            Map<Integer, BigDecimal> revenueMap = new HashMap<>();
            for (Object[] row : revenueResults) {
                Integer beneficiaryId = ((Number) row[0]).intValue();
                BigDecimal total = (row[1] instanceof BigDecimal) ? (BigDecimal) row[1] : new BigDecimal(row[1].toString());
                revenueMap.put(beneficiaryId, total);
            }

            LocalDate prevStartDate, prevEndDate;
            switch (period) {
                case "week":
                    prevStartDate = startDate.minusWeeks(1);
                    prevEndDate = startDate.minusDays(1);
                    break;
                case "month":
                    prevStartDate = startDate.minusMonths(1);
                    prevEndDate = startDate.minusDays(1);
                    break;
                case "quarter":
                    prevStartDate = startDate.minusMonths(3);
                    prevEndDate = startDate.minusDays(1);
                    break;
                case "year":
                    prevStartDate = startDate.minusYears(1);
                    prevEndDate = startDate.minusDays(1);
                    break;
                default:
                    prevStartDate = LocalDate.of(2020, 1, 1);
                    prevEndDate = startDate.minusDays(1);
            }
            String prevSql = "SELECT beneficiary_id, SUM(amount) as total " +
                    "FROM revenue " +
                    "WHERE beneficiary_type = 'TRANSPORT_UNIT' " +
                    "AND date >= :startDate AND date <= :endDate " +
                    "GROUP BY beneficiary_id";
            Query prevQuery = entityManager.createNativeQuery(prevSql);
            prevQuery.setParameter("startDate", prevStartDate);
            prevQuery.setParameter("endDate", prevEndDate);
            List<Object[]> prevRevenueResults = prevQuery.getResultList();
            Map<Integer, BigDecimal> prevRevenueMap = new HashMap<>();
            for (Object[] row : prevRevenueResults) {
                Integer beneficiaryId = ((Number) row[0]).intValue();
                BigDecimal total = (row[1] instanceof BigDecimal) ? (BigDecimal) row[1] : new BigDecimal(row[1].toString());
                prevRevenueMap.put(beneficiaryId, total);
            }

            Map<String, TransportUnit> uniqueUnitMap = new LinkedHashMap<>();
            for (TransportUnit unit : units) {
                long bookingCount = bookingRepository.findByTransportUnit(unit).size();
                if (bookingCount == 0) continue;
                String name = unit.getNameCompany();
                if (!uniqueUnitMap.containsKey(name)) {
                    uniqueUnitMap.put(name, unit);
                } else {
                    TransportUnit current = uniqueUnitMap.get(name);
                    long currentBookings = bookingRepository.findByTransportUnit(current).size();
                    if (bookingCount > currentBookings) {
                        uniqueUnitMap.put(name, unit);
                    }
                }
            }
            List<TransportUnit> uniqueUnits = new ArrayList<>(uniqueUnitMap.values());

            List<TeamRankingResponse> result = new ArrayList<>();

            for (TransportUnit unit : uniqueUnits) {
                System.out.println("[DEBUG] RevenueMap keys: " + revenueMap.keySet());
                System.out.println("[DEBUG] Current unit: " + unit.getNameCompany() + ", id: " + unit.getTransportId());
                System.out.println("[DEBUG] Revenue for this unit: " + revenueMap.get(unit.getTransportId().intValue()));

                List<Booking> allUnitBookings = bookingRepository.findByTransportUnit(unit);
                List<Booking> currentBookings = allUnitBookings.stream()
                        .filter(b -> {
                            LocalDate deliveryDate = b.getDeliveryDate() != null ? b.getDeliveryDate().toLocalDate() : null;
                            LocalDate createdAt = b.getCreatedAt() != null ? b.getCreatedAt().toLocalDate() : null;
                            boolean inRange = false;
                            if (deliveryDate != null) {
                                inRange = !deliveryDate.isBefore(startDate) && !deliveryDate.isAfter(extendedEndDate);
                            }
                            if (!inRange && createdAt != null) {
                                inRange = !createdAt.isBefore(startDate) && !createdAt.isAfter(extendedEndDate);
                            }
                            return inRange;
                        })
                        .collect(Collectors.toList());

                BigDecimal totalRevenue = revenueMap.getOrDefault(unit.getTransportId().intValue(), BigDecimal.ZERO);
                int totalTrips = currentBookings.size();

                long completedBookings = currentBookings.stream()
                        .filter(b -> "COMPLETED".equals(b.getStatus()))
                        .count();
                double avgSuccessRate = totalTrips == 0 ? 0.0 : (double) completedBookings / totalTrips * 100;

                Set<OperatorStaff> staffSet = new HashSet<>();
                for (Booking booking : currentBookings) {
                    if (booking.getOperatorStaff() != null) {
                        staffSet.add(booking.getOperatorStaff());
                    }
                }
                int members = staffSet.size();

                List<Booking> prevBookings = allUnitBookings.stream()
                        .filter(b -> {
                            if (b.getDeliveryDate() == null) return false;
                            LocalDate bookingDate = b.getDeliveryDate().toLocalDate();
                            return !bookingDate.isBefore(prevStartDate) && !bookingDate.isAfter(prevEndDate);
                        })
                        .collect(Collectors.toList());
                BigDecimal prevTotalRevenue = prevRevenueMap.getOrDefault(unit.getTransportId().intValue(), BigDecimal.ZERO);

                int prevTotalTrips = prevBookings.size();
                long prevCompletedBookings = prevBookings.stream()
                        .filter(b -> "COMPLETED".equals(b.getStatus()))
                        .count();
                double prevAvgSuccessRate = prevTotalTrips == 0 ? 0.0 : (double) prevCompletedBookings / prevTotalTrips * 100;

                double prevValue = 0;
                double currentValue = 0;
                double changePercentage = 0;
                String trend = "up";

                switch (metric.toLowerCase()) {
                    case "revenue":
                        prevValue = prevTotalRevenue.doubleValue();
                        currentValue = totalRevenue.doubleValue();
                        break;
                    case "trips":
                        prevValue = prevTotalTrips;
                        currentValue = totalTrips;
                        break;
                    case "success_rate":
                        prevValue = prevAvgSuccessRate;
                        currentValue = avgSuccessRate;
                        break;
                    default:
                        prevValue = prevTotalRevenue.doubleValue();
                        currentValue = totalRevenue.doubleValue();
                }

                if (prevValue == 0) {
                    changePercentage = currentValue > 0 ? 100.0 : 0.0;
                } else {
                    changePercentage = (currentValue - prevValue) / prevValue * 100;
                }

                trend = changePercentage >= 0 ? "up" : "down";

                String formattedRevenue = totalRevenue.compareTo(BigDecimal.ZERO) == 0 ? "0 đ" : DECIMAL_FORMAT.format(totalRevenue);
                String formattedChange = String.format("%.1f%%", Math.abs(changePercentage));

                TeamRankingResponse response = new TeamRankingResponse();
                response.setName(unit.getNameCompany() != null ? unit.getNameCompany() : "Unknown Company");
                response.setTotalRevenue(formattedRevenue);
                response.setTotalTrips(totalTrips);
                response.setAvgSuccessRate(Math.round(avgSuccessRate * 100.0) / 100.0);
                response.setMembers(members);
                response.setChange(formattedChange);
                response.setTrend(trend);

                result.add(response);
            }

            result.sort((a, b) -> {
                switch (metric.toLowerCase()) {
                    case "revenue":
                        try {
                            double revenueA = Double.parseDouble(a.getTotalRevenue().replace(",", "").replace(" đ", ""));
                            double revenueB = Double.parseDouble(b.getTotalRevenue().replace(",", "").replace(" đ", ""));
                            return Double.compare(revenueB, revenueA);
                        } catch (NumberFormatException e) {
                            return 0;
                        }
                    case "trips":
                        return Integer.compare(b.getTotalTrips(), a.getTotalTrips());
                    case "success_rate":
                        return Double.compare(b.getAvgSuccessRate(), a.getAvgSuccessRate());
                    default:
                        try {
                            double revenueA = Double.parseDouble(a.getTotalRevenue().replace(",", "").replace(" đ", ""));
                            double revenueB = Double.parseDouble(b.getTotalRevenue().replace(",", "").replace(" đ", ""));
                            return Double.compare(revenueB, revenueA);
                        } catch (NumberFormatException e) {
                            return 0;
                        }
                }
            });

            Map<String, TeamRankingResponse> uniqueCompanyMap = new LinkedHashMap<>();
            for (TeamRankingResponse resp : result) {
                String company = resp.getName();
                if (!uniqueCompanyMap.containsKey(company)) {
                    uniqueCompanyMap.put(company, resp);
                }
            }
            List<TeamRankingResponse> uniqueCompanyList = new ArrayList<>(uniqueCompanyMap.values());

            List<TeamRankingResponse> top3 = uniqueCompanyList.stream()
                    .limit(3)
                    .collect(Collectors.toList());
            for (int i = 0; i < top3.size(); i++) {
                top3.get(i).setRank(i + 1);
            }

            return top3;

        } catch (Exception e) {
            System.err.println("Error in getTeamRanking: " + e.getMessage());
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
                OperatorStaff topRevenueStaff = staffList.stream()
                        .max(Comparator.comparing(s -> revenueRepository.findAll().stream()
                                .filter(r -> "OPERATOR_STAFF".equals(r.getBeneficiaryType())
                                        && r.getBeneficiaryId() != null
                                        && r.getBeneficiaryId().intValue() == s.getOperatorId().intValue()
                                        && r.getDate() != null
                                        && !r.getDate().isBefore(startDate)
                                        && !r.getDate().isAfter(endDate))
                                .map(r -> r.getAmount() != null ? r.getAmount() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                        ))
                        .orElse(null);
                if (topRevenueStaff != null) {
                    BigDecimal totalRevenue = revenueRepository.findAll().stream()
                            .filter(r -> "OPERATOR_STAFF".equals(r.getBeneficiaryType())
                                    && r.getBeneficiaryId() != null
                                    && r.getBeneficiaryId().intValue() == topRevenueStaff.getOperatorId().intValue()
                                    && r.getDate() != null
                                    && !r.getDate().isBefore(startDate)
                                    && !r.getDate().isAfter(endDate))
                            .map(r -> r.getAmount() != null ? r.getAmount() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    AchievementResponse revenueAchievement = new AchievementResponse();
                    revenueAchievement.setValue(DECIMAL_FORMAT.format(totalRevenue));
                    revenueAchievement.setLabel("Tổng doanh thu cao nhất");
                    revenueAchievement.setName(topRevenueStaff.getUsers().getFullName());
                    result.add(revenueAchievement);
                }
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