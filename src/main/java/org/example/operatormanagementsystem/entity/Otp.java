package org.example.operatormanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "otp")
@ToString(of = {"id", "email", "otp", "expiredTime"})
public class Otp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users users;

    @Column(length = 100)
    private String email;


        @Column(name = "created_date")
//         Thời điểm bản ghi được tạo ra lần đầu trong database.
        private LocalDateTime createdDate;


        @Column(name = "updated_date")
//         Thời điểm bản ghi được cập nhật lần cuối.
        private LocalDateTime updatedDate;

        @Column(name = "expired_time", nullable = false)
        private LocalDateTime expiredTime;
//         Thời điểm hết hiệu lực của bản ghi đó

    @Column(length = 20, nullable = false)
    private String otp;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdDate == null) createdDate = now;
        if (updatedDate == null) updatedDate = now;
        if (expiredTime == null) expiredTime = now.plusMinutes(1);
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
        expiredTime = updatedDate.plusMinutes(1); // Cập nhật hiệu lực sau 1 phút mỗi lần update
    }

}
