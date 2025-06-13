package org.example.operatormanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.operatormanagementsystem.enumeration.UserRole;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "feedback")
@ToString(of = {"feedbackId", "content"})
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Integer feedbackId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private OperatorStaff operatorStaff;

    // Trong Entity Feedback.java (bạn đã cung cấp)
    @ManyToOne(fetch = FetchType.LAZY   )
    @JoinColumn(name = "manager_id") // <-- Đây là trường "manager" trong Feedback
    private Manager manager; // <-- Nó có tồn tại!

    @Column(length = 500)
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @JoinColumn(name = "sender_user_id", nullable = false) // sender_user_id là cột FK trong bảng feedback
    @Enumerated(EnumType.STRING)
    private UserRole senderUser;


    @JoinColumn(name = "receiver_user_id", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole receiverUser;

    @Column(name = "process_status")
    private LocalDateTime processStatus;

    @Column(name = "order_id")
    private Integer orderId;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}