package org.example.operatormanagementsystem.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "issue_log")
@ToString(of = {"issueId", "reporterRole", "status"})
public class IssueLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "issue_id")
    private Integer issueId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id") // FK column name in 'issue_log' table
    private Booking booking;

    @Column(name = "reported_by")
    private Integer reportedBy;

    @Column(name = "reporter_role", length = 50)
    private String reporterRole;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "status", length = 50)
    private String status;

    //@CreatedDate
    //@Column(name = "created_date", updatable = false)
    //@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    //private LocalDateTime createdDate;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;

    @Column(name = "solved_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime solvedAt;

//    @PrePersist
//    protected void onCreate() {
//        if (this.createdAt == null) {
//            this.createdAt = LocalDateTime.now();
//        }
//    }
}