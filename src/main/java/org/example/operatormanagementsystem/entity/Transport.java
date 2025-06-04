package org.example.operatormanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "transport") // SQL table name
@ToString(of = {"progressId", "status", "updatedAt"})
public class Transport { // Class name from your image

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_id") // SQL PK column name
    private Integer progressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id") // FK column name in 'transport' table
    private Booking booking;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "note", length = 255)
    private String note;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}