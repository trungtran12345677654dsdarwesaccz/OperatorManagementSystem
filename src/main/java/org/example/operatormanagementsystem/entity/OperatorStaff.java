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
@Table(name = "operator_staff")
@ToString(of = {"operatorId", "fullname", "username"})
public class OperatorStaff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "operator_id")
    private Integer operatorId;

    @Column(nullable = false, length = 100)
    private String fullname;

    @Column(nullable = false, length = 50, unique = true)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 30)
    private String role;

    @Column(length = 20)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Manager manager;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private Users users;

    @OneToMany(mappedBy = "operatorStaff", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Booking> bookings;

    @OneToMany(mappedBy = "operatorStaff", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Feedback> feedbacks;

    @OneToMany(mappedBy = "operatorStaff", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ChatbotLog> chatbotLogs;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
