package org.example.operatormanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "transport_unit")
@ToString(of = {"transportId", "name", "licensePlate"})
public class TransportUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transport_id")
    private Integer transportId;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "driver_name", length = 100)
    private String driverName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "license_plate", length = 20)
    private String licensePlate;

    @Column(name = "status", length = 30)
    private String status;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "transportUnit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Booking> bookings;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}