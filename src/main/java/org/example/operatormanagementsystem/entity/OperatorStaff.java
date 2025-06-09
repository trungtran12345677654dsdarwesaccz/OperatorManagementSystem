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
@Table(name = "operator_staff")
@ToString(of = {"operatorId"})
public class OperatorStaff {

    @Id
    @Column(name = "operator_id")
    private Integer operatorId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "operator_id")
    private Users users;

    @Column(name = "admin_id") // Simple integer ID, no explicit FK in provided DDL
    private Integer adminId;

    @OneToMany(mappedBy = "operatorStaff", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Booking> bookings;

    @OneToMany(mappedBy = "operatorStaff", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ChatbotLog> chatbotLogs;

    @OneToMany(mappedBy = "operatorStaff", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Feedback> feedbacks;

}

