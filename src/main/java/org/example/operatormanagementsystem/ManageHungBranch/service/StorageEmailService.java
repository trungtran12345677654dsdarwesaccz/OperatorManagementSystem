package org.example.operatormanagementsystem.ManageHungBranch.service;

import jakarta.mail.MessagingException;
import org.example.operatormanagementsystem.enumeration.ApprovalStatus;

public interface StorageEmailService {
    /**
     * Gửi email thông báo kết quả duyệt kho.
     *
     * @param recipientEmail Email người nhận (người đã gửi yêu cầu)
     * @param recipientName  Tên người nhận (để xưng hô, ví dụ: "Chào bạn")
     * @param status         Trạng thái duyệt (APPROVED hoặc REJECTED)
     * @param managerNote    Ghi chú của người quản lý (đặc biệt quan trọng khi từ chối)
     */
    void sendStorageUnitApprovalNotification(
            String recipientEmail,
            String recipientName,
            ApprovalStatus status,
            String managerNote) throws MessagingException
    ;
}

