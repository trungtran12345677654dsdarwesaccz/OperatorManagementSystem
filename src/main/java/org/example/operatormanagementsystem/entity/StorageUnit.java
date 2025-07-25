package org.example.operatormanagementsystem.entity;



import lombok.Getter;
import lombok.Setter;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
public class StorageUnit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "storage_id")
    private Integer storageId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Manager manager;

    @Column(length = 20)
    private String phone;

    @Column(length = 30)
    private String status;

    @Column(length = 255)
    private String note;

    @Column(length = 255) // Thay đổi từ Double sang String và tăng độ dài
    private String image; // URL hoặc path của ảnh

    @Column(name="slot_count", nullable=false)
    private Integer slotCount;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false, columnDefinition = "DATETIME2 DEFAULT GETDATE()")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "storageUnit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default // Chỉ có tác dụng với Lombok Builder!
    private Set<Booking> bookings = new HashSet<>();



    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
