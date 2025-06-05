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
@Table(name = "username", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username")
})
@ToString(of = {"id", "username", "email"})
public class UserName { // Class name from your image

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 100) // U
    private String password;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "fullName", length = 100)
    private String fullName;

    @Column(length = 50)
    private String role;

    @Column(name = "created_Date")
    private LocalDateTime createdDate;

    @Column(length = 20)
    private String status;

    @OneToOne(mappedBy = "username", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Customer customers;

    @OneToOne(mappedBy = "username", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Manager manager;

    @OneToOne(mappedBy = "username", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private OperatorStaff operatorStaff;

    @OneToOne(mappedBy = "username", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private OperatorStaff users;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}