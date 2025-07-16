    package org.example.operatormanagementsystem.payment.dto.request;

    import jakarta.validation.constraints.Min;
    import jakarta.validation.constraints.NotNull;
    import jakarta.validation.constraints.Size;
    import lombok.AllArgsConstructor;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import lombok.Setter;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public class CreatePaymentRequest {


        private Integer bookingId;


        private Long amount;


        private String note;
    }
