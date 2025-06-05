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
@Table(name = "manager", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username")
})
@ToString(of = {"managerId", "username"})
public class Manager {

    @Id // No @GeneratedValue because manager_id gets its value from Users.id
    @Column(name = "manager_id")
    private Integer managerId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // This annotation ensures that 'managerId' (PK of Manager) is populated with the ID of the associated 'Users' entity.
    @JoinColumn(name = "manager_id") // Specifies that 'manager_id' column is used for both PK and FK.
    private Users user;

    @Column(name = "fullname", nullable = false, length = 100) // Potentially redundant if Users has fullname
    private String fullname;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 100) // Should be password_hash
    private String password;

    @Column(name = "email", length = 100) // Potentially redundant
    private String email;

    @Column(name = "phone", length = 20) // Potentially redundant
    private String phone;

    // CHK_Manager_Status CHECK  (([status]='retired' OR [status]='inactive' OR [status]='active'))
    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    UserStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "manager", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<CustomerService> customerServices;

    @OneToMany(mappedBy = "manager", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<OperatorStaff> managedOperatorStaffs; // Renamed to avoid conflict if OperatorStaff also has a manager field for a different purpose

    @OneToMany(mappedBy = "manager", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<StorageUnit> storageUnits;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        // Sync from User if needed, e.g., upon creation if these fields should match Users
        // if (this.user != null) {
        //     this.fullname = this.user.getFullName();
        //     this.email = this.user.getEmail();
        //     this.phone = this.user.getPhone();
        // }
    }
}