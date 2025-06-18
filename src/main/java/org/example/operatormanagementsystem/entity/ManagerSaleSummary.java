package org.example.operatormanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal; // Sử dụng BigDecimal cho tiền tệ để tránh lỗi làm tròn
import java.time.LocalDate; // Sử dụng LocalDate cho ngày để chỉ quan tâm đến ngày, không phải giờ
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "manager_sale_summary")
public class ManagerSaleSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "summary_id")
    private Long summaryId;

    // Mối quan hệ Many-to-One với Manager (ai là chủ của bản thống kê này)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    private Manager manager;

    @Column(name = "total_revenue", precision = 19, scale = 2, nullable = false) // Tổng doanh thu
    private BigDecimal totalRevenue; // Precision và Scale để quản lý số thập phân

    @Column(name = "total_bookings", nullable = false) // Tổng số booking
    private Integer totalBookings;

    @Column(name = "period_start_date", nullable = false) // Ngày bắt đầu kỳ thống kê
    private LocalDate periodStartDate;

    @Column(name = "period_end_date", nullable = false) // Ngày kết thúc kỳ thống kê
    private LocalDate periodEndDate;

    @CreatedDate
    @Column(name = "generated_at", updatable = false) // Thời gian bản ghi thống kê được tạo
    private LocalDateTime generatedAt;

    // Các trường khác có thể thêm vào tùy theo nhu cầu thống kê
    // ví dụ: total_customers_served, average_revenue_per_booking, etc.

    @PrePersist
    protected void onCreate() {
        if (generatedAt == null) {
            generatedAt = LocalDateTime.now();
        }
    }
}
