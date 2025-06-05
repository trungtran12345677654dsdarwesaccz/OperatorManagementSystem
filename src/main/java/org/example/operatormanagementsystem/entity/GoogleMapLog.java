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
@Table(name = "ggmaps_log")
@ToString(of = {"ggmapsId", "requestedAt"})
public class GoogleMapLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ggmaps_id")
    private Integer ggmapsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id") // FK column name in 'ggmaps_log' table
    private Booking booking;

    @Column(name = "address_from", length = 255)
    private String addressFrom;

    @Column(name = "address_to", length = 255)
    private String addressTo;

    @Column(name = "map_url", length = 500)
    private String mapUrl;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private OperatorStaff operatorStaff;

    @PrePersist
    protected void onRequest() {
        if (this.requestedAt == null) {
            this.requestedAt = LocalDateTime.now();
        }
    }
}