
package org.example.operatormanagementsystem.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "debt")
@ToString(of = {"debtId", "debtorType", "amount", "status"})
public class Debt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "debt_id")
    private Integer debtId;

    @Column(name = "debtor_type", length = 30)
    private String debtorType;

    @Column(name = "debtor_id")
    private Integer debtorId;

    @Column(name = "creditor_type", length = 30)
    private String creditorType;

    @Column(name = "creditor_id")
    private Integer creditorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Column(name = "amount", precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "description", length = 255)
    private String description;

    @CreatedDate
    //@Column(name = "created_date", updatable = false)
    //@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    //private LocalDateTime createdDate;
    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;

    @Column(name = "status", length = 50)
    private String status;

//    @PrePersist
//    protected void onCreate() {
//        if (this.createdAt == null) {
//            this.createdAt = LocalDateTime.now();
//        }
//    }
}
