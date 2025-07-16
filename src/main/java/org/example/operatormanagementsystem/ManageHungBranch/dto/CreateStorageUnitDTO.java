package org.example.operatormanagementsystem.ManageHungBranch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

// DTO riêng cho việc tạo mới (không cần storageId và createdAt)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateStorageUnitDTO {

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

    @NotNull(message = "Số lượng ô không được để trống")
    private Integer slotCount;

    private List<Integer> bookedSlots;
}
