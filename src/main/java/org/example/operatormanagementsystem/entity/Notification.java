package org.example.operatormanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notification")
@ToString(of = {"gmailId", "sentTo", "subject"})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gmail_id") // Primary key in SQL
    private Integer gmailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id") // FK column name in 'notification' table
    private Booking booking;

    @Column(name = "sent_to", length = 100)
    private String sentTo;

    @Column(name = "subject", length = 255)
    private String subject;

    @Lob
    @Column(name = "content", columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @PrePersist
    protected void onSend() {
        if (this.sentAt == null) {
            this.sentAt = LocalDateTime.now();
        }
    }
}