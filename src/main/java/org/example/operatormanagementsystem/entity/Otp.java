package org.example.operatormanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "otp")
@ToString(of = {"id", "email", "otp", "expiredTime", "status"}) // Thêm 'status' vào toString
public class Otp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users users;

    @Column(length = 100, nullable = false) // Đảm bảo email không null
    private String email;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "expired_time", nullable = false)
    private LocalDateTime expiredTime;

    @Column(length = 20, nullable = false)
    private String otp;

    @Enumerated(EnumType.STRING) // Thêm trường trạng thái cho OTP
    @Column(name = "status", nullable = false)
    private OtpStatus status; // Sử dụng một Enum OtpStatus

    // Enum cho trạng thái OTP
    public enum OtpStatus {
        PENDING,    // Đang chờ xác minh
        VERIFIED,   // Đã xác minh thành công
        EXPIRED,    // Đã hết hạn (do hết thời gian)
        USED        // Đã sử dụng (do nhập sai hoặc đã được dùng)
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdDate == null) createdDate = now;
        if (updatedDate == null) updatedDate = now;
        if (expiredTime == null) expiredTime = now.plusMinutes(1); // Mặc định hết hạn sau 1 phút
        if (status == null) status = OtpStatus.PENDING; // Mặc định trạng thái là PENDING khi tạo mới
    }

    @PreUpdate
    protected void onUpdate() {
        // Chỉ cập nhật updatedDate. Logic hết hạn và trạng thái sẽ được quản lý ở service
        updatedDate = LocalDateTime.now();
        // expiredTime không nên được reset ở đây, vì nó chỉ nên hết hạn dựa trên logic trong service
        // và trạng thái OtpStatus.EXPIRED cũng được đặt trong service
    }
}
