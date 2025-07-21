package org.example.operatormanagementsystem.enumeration;

public enum StorageStatus {
    PENDING_APPROVAL, // Chờ duyệt của Manager
    ACTIVE,           // Được duyệt, đang hoạt động
    INACTIVE,         // Ngừng hoạt động (do khóa/xóa/tạm dừng)
    REJECTED          // Bị từ chối duyệt
}
