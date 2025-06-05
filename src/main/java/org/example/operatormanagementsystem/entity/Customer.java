package org.example.operatormanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.operatormanagementsystem.enumeration.UserStatus;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "customer", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email") // email in customer table is also unique
})
@ToString(of = {"customerId", "fullname", "email"})
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Integer customerId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // This annotation ensures that 'managerId' (PK of Manager) is populated with the ID of the associated 'Users' entity.
    @JoinColumn(name = "customer_id") // Specifies that 'manager_id' column is used for both PK and FK.
    private Users user;

    @Column(name = "fullname", nullable = false, length = 100)
    private String fullname;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "city", length = 50)
    private String city;

    @Column(name = "district", length = 50)
    private String district;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // CHK_Customer_Status CHECK  (([status]='suspended' OR [status]='inactive' OR [status]='active'))
    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    UserStatus status;



    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Booking> bookings;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ChatbotLog> chatbotLogs;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Feedback> feedbacks;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
