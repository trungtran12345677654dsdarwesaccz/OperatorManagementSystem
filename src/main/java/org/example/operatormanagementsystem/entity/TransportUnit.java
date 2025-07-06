package org.example.operatormanagementsystem.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.example.operatormanagementsystem.enumeration.TransportAvailabilityStatus;
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

    @Column(name = "image", length = 500)
    private String imageTransportUnit;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "transportUnit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Booking> bookings;

    @Column(name = "number_of_vehicles")
    private Integer numberOfVehicles;


    @Column(name = "capacity_per_vehicle")
    private Double capacityPerVehicle;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status", length = 20)
    private TransportAvailabilityStatus availabilityStatus;

    @Column(name = "certificate_front_url", length = 500)
    private String certificateFrontUrl;

    @Column(name = "certificate_back_url", length = 500)
    private String certificateBackUrl;

    @OneToOne(mappedBy = "transportUnit", cascade = CascadeType.ALL)
    private TransportUnitApproval approval;



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