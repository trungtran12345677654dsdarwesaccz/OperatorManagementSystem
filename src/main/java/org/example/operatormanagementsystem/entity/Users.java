package org.example.operatormanagementsystem.entity;

<<<<<<< Updated upstream

import jakarta.persistence.*;
import lombok.*;
=======
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

>>>>>>> Stashed changes
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
})
@ToString(of = {"id", "fullName", "email"})
public class Users { // Class name from your image

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "full_name", nullable = false, length = 50)
    private String fullName;

<<<<<<< Updated upstream
    @Column(name = "email", nullable = false, length = 100) // Uniqueness handled by @Table
=======



    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "email", nullable = false, length = 100) // Uniqueness handled by @Table

>>>>>>> Stashed changes
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Customer> customers;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Manager manager;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private OperatorStaff operatorStaff;

<<<<<<< Updated upstream
=======


    @CreatedDate
    @Column(updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdDate;




>>>>>>> Stashed changes
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
