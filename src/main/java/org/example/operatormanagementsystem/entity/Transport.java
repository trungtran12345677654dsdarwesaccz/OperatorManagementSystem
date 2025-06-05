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
@Table(name = "transport") // SQL table name
@ToString(of = {"progressId", "status", "updatedAt"})
public class Transport { // Class name from your image

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_id") // SQL PK column name
    private Integer progressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id") // FK column name in 'transport' table
    private Booking booking;

    @Column(name = "status", length = 50)
    private String status;

    @CreatedDate
    @Column(name = "updated_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updatedAt;
//@CreatedDate
//@Column(name = "created_date", updatable = false)
//@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
//private LocalDateTime createdDate;
    @Column(name = "note", length = 255)
    private String note;


}