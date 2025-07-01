package org.example.operatormanagementsystem.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "`position`", uniqueConstraints = {
        @UniqueConstraint(columnNames = "title"),
        @UniqueConstraint(columnNames = "user_id")
})
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "position_id", nullable = false, columnDefinition = "INT")
    private Integer positionId;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "secondary_title", length = 100)
    private String secondaryTitle;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_salary", precision = 18, scale = 2, nullable = false)
    private BigDecimal baseSalary;

    @Column(name = "status", length = 30, nullable = false)
    private String status;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;
}