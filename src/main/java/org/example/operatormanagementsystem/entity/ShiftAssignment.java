package org.example.operatormanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.operatormanagementsystem.enumeration.ShiftAssignmentStatus;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "shift_assignment", 
       uniqueConstraints = @UniqueConstraint(
           name = "unique_assignment", 
           columnNames = {"operator_id", "assignment_date", "shift_id"}
       ))
public class ShiftAssignment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private Integer assignmentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", nullable = false)
    private Users operator;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", nullable = false)
    private WorkShift workShift;
    
    @Column(name = "assignment_date", nullable = false)
    private LocalDate assignmentDate;
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ShiftAssignmentStatus status = ShiftAssignmentStatus.ASSIGNED;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}