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
@Table(name = "operator_staff", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username")
})
@ToString(of = {"operatorId", "username", "role"})
public class OperatorStaff {

    @Id // No @GeneratedValue, value comes from Users.id
    @Column(name = "operator_id")
    private Integer operatorId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "operator_id")
    private Users user;

    @Column(name = "fullname", nullable = false, length = 100)
    private String fullname;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "role", length = 30)
    private String role;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "admin_id") // Simple integer ID, no explicit FK in provided DDL
    private Integer adminId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id") // FK to Manager table
    private Manager manager;

    @OneToMany(mappedBy = "operatorStaff", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Booking> bookings;

    @OneToMany(mappedBy = "operatorStaff", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ChatbotLog> chatbotLogs;

    @OneToMany(mappedBy = "operatorStaff", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Feedback> feedbacks;

    @OneToMany(mappedBy = "operatorStaff", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<GoogleMapLog> googleMapLogs;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
