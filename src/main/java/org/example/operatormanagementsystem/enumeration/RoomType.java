package org.example.operatormanagementsystem.enumeration;

public enum RoomType {
    LIVING_ROOM("Phòng khách"),
    DINING_ROOM("Phòng ăn"),
    BEDROOM("Phòng ngủ"),
    BATHROOM("Phòng tắm"),
    OTHER("Khác");

    private final String displayName;

    RoomType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 