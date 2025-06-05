package org.example.operatormanagementsystem.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "manager", uniqueConstraints = {
        @UniqueConstraint(columnNames = "manager_id")
})
@ToString(of = {"managerId"})
public class Manager {

    @Id // No @GeneratedValue because manager_id gets its value from Users.id
    @Column(name = "manager_id")
    private Integer managerId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // This annotation ensures that 'managerId' (PK of Manager) is populated with the ID of the associated 'Users' entity.
    @JoinColumn(name = "id") // Specifies that 'manager_id' column is used for both PK and FK.
    private Users users;


    @OneToMany(mappedBy = "manager", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<CustomerService> customerServices;

    @OneToMany(mappedBy = "manager", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<OperatorStaff> managedOperatorStaffs; // Renamed to avoid conflict if OperatorStaff also has a manager field for a different purpose

    @OneToMany(mappedBy = "manager", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<StorageUnit> storageUnits;



}