package org.example.operatormanagementsystem.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity

public class ManagerFeedbackToStaff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private int feedbackId; // Sử dụng Long cho ID để linh hoạt hơn

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false) // Khóa ngoại trỏ đến Manager
    private Manager manager; // Manager đưa ra phản hồi

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", nullable = false) // Khóa ngoại trỏ đến OperatorStaff
    private OperatorStaff operatorStaff; // OperatorStaff nhận phản hồi

    @Column(name = "feedback_content", length = 1000) // Nội dung phản hồi
    private String feedbackContent;

    @Column(name = "rating") // Đánh giá (ví dụ: từ 1-5 hoặc 1-10)
    private Integer rating; // Có thể dùng Float/Double nếu muốn điểm lẻ

    @CreationTimestamp
    @Column(name = "created_at", updatable = false,  columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}