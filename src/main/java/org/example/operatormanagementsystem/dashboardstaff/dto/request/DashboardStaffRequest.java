package org.example.operatormanagementsystem.dashboardstaff.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStaffRequest {
    @NotBlank(message = "Tên chức vụ không được để trống")
    private String title;

    private String secondaryTitle;

    private String description;

    @NotNull(message = "Lương cơ bản không được để trống")
    private BigDecimal baseSalary;

    @NotBlank(message = "Trạng thái không được để trống")
    private String status;

    @NotNull(message = "ID người dùng không được để trống")
    private Integer userId; // Thêm trường userId
}