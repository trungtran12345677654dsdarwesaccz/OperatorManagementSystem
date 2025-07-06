package org.example.operatormanagementsystem.enumeration;

public enum TransportAvailabilityStatus {
    AVAILABLE,         // Đáp ứng được yêu cầu (còn đủ xe)
    INSUFFICIENT,      // Còn xe nhưng KHÔNG đủ đáp ứng yêu cầu
    FULLY_BUSY         // Hết sạch xe (tất cả đang được sử dụng)
}
