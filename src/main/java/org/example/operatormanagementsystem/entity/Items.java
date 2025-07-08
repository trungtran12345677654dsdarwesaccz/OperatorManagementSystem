package org.example.operatormanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.operatormanagementsystem.enumeration.RoomType;

@Entity
@Table(name = "items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Items {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Integer itemId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "weight", nullable = false)
    private Double weight; // kg

    @Column(name = "volume", nullable = false)
    private Double volume; // m³

    @Column(name = "modular", nullable = false)
    private Boolean modular; // có thể tháo lắp

    @Column(name = "bulky", nullable = false)
    private Boolean bulky; // cồng kềnh cần nhân lực hỗ trợ

    @Column(name = "room", nullable = false)
    @Enumerated(EnumType.STRING)
    private RoomType room; // loại phòng: phòng khách, phòng ăn, phòng ngủ, phòng tắm, khác

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;
} 