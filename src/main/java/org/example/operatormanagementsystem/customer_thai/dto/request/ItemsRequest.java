package org.example.operatormanagementsystem.customer_thai.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.operatormanagementsystem.enumeration.RoomType;

@Data
public class ItemsRequest {
    @NotBlank(message = "Item name is required")
    private String name;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Weight is required")
    @Min(value = 0, message = "Weight must be non-negative")
    private Double weight;

    @NotNull(message = "Volume is required")
    @Min(value = 0, message = "Volume must be non-negative")
    private Double volume;

    @NotNull(message = "Modular flag is required")
    private Boolean modular;

    @NotNull(message = "Bulky flag is required")
    private Boolean bulky;

    @NotNull(message = "Room type is required")
    private RoomType room;
} 