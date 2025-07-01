package org.example.operatormanagementsystem.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.example.operatormanagementsystem.enumeration.UserStatus;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class TransportUnit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transport_id")
    private Integer transportId;

    @Column(length = 100)
    private String nameCompany;

    @Column(length = 100)
    private String namePersonContact;


    @Column(length = 20)
    private String phone;


    @Column(name = "license_plate", length = 20)
    private String licensePlate;

    @Column(length = 30)
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Column(length = 255)
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "transportUnit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Booking> bookings;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = UserStatus.PENDING_APPROVAL; // Hoặc một trạng thái mặc định khác tùy theo logic của bạn
        }
    }
}