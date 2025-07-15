package org.example.operatormanagementsystem.payment.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePaymentRequest {

    @NotNull(message = "Booking ID không được để trống")
    private Integer bookingId;

    @Min(value = 1, message = "Số tiền phải lớn hơn 0")
    private Long amount;

    @Size(max = 255, message = "Ghi chú tối đa 255 ký tự")
    private String note;  // Tùy chọn ghi chú
}
