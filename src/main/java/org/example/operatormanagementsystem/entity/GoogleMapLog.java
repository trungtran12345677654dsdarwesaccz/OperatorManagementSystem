package org.example.operatormanagementsystem.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

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

    //@CreatedDate
    //@Column(name = "created_date", updatable = false)
    //@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    //private LocalDateTime createdDate;
    @CreatedDate
    @Column(name = "requested_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime requestedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private OperatorStaff operatorStaff;

//    @PrePersist
//    protected void onRequest() {
//        if (this.requestedAt == null) {
//            this.requestedAt = LocalDateTime.now();
//        }
//    }
}