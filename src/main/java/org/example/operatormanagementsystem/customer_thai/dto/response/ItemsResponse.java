package org.example.operatormanagementsystem.customer_thai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.operatormanagementsystem.enumeration.RoomType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemsResponse {
    private Integer itemId;
    private String name;
    private Integer quantity;
    private Double weight;
    private Double volume;
    private Boolean modular;
    private Boolean bulky;
    private RoomType room;
    private Integer bookingId;
} 