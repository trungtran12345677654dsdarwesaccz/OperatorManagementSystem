package org.example.operatormanagementsystem.ManageHungBranch.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorageUnitDTO {

    private Integer storageId;

    @NotBlank(message = "Tên kho không được để trống")
    @Size(max = 100, message = "Tên kho không được vượt quá 100 ký tự")
    private String name;

    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String address;

    private Integer managerId;

    private String managerName; // Tên của manager để hiển thị

    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String phone;

    @Size(max = 30, message = "Trạng thái không được vượt quá 30 ký tự")
    private String status;

    @Size(max = 255, message = "Ghi chú không được vượt quá 255 ký tự")
    private String note;

    @Size(max = 255, message = "Link ảnh phải có dạng .png .jpg .jpeg")
    private String image;

    private Integer slotCount;
    private List<Integer> bookedSlots;   // chỉ số các ô đã full

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}

// DTO riêng cho việc tạo mới (không cần storageId và createdAt)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class CreateStorageUnitDTO {

    @NotBlank(message = "Tên kho không được để trống")
    @Size(max = 100, message = "Tên kho không được vượt quá 100 ký tự")
    private String name;

    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String address;

    private Integer managerId;

    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String phone;

    @Size(max = 30, message = "Trạng thái không được vượt quá 30 ký tự")
    private String status;

    @Size(max = 255, message = "Ghi chú không được vượt quá 255 ký tự")
    private String note;

    @Size(max = 500, message = "Đường dẫn ảnh không được vượt quá 500 ký tự")
    private String image;

    private Integer slotCount;
    private List<Integer> bookedSlots;
}

// DTO riêng cho việc cập nhật
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class UpdateStorageUnitDTO {

    @Size(max = 100, message = "Tên kho không được vượt quá 100 ký tự")
    private String name;

    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String address;

    private Integer managerId;

    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String phone;

    @Size(max = 30, message = "Trạng thái không được vượt quá 30 ký tự")
    private String status;

    @Size(max = 255, message = "Ghi chú không được vượt quá 255 ký tự")
    private String note;

    @Size(max = 255, message = "Link ảnh phải có dạng .png .jpg .jpeg")
    private String image;

    private Integer slotCount;
    private List<Integer> bookedSlots;

}