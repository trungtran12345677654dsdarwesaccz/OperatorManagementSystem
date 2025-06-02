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
@Table(name = "chatbot_log")
@ToString(of = {"chatbotId", "askedAt"})
public class ChatbotLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chatbot_id")
    private Integer chatbotId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private OperatorStaff operatorStaff;

    @Lob
    @Column(name = "question", columnDefinition = "NVARCHAR(MAX)")
    private String question;

    @Lob
    @Column(name = "response", columnDefinition = "NVARCHAR(MAX)")
    private String response;

    @Column(name = "asked_at")
    private LocalDateTime askedAt;

    @PrePersist
    protected void onAsk() {
        if (this.askedAt == null) {
            this.askedAt = LocalDateTime.now();
        }
    }
}
