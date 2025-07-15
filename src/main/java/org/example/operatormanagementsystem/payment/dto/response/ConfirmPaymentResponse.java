package org.example.operatormanagementsystem.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmPaymentResponse {

    @JsonProperty("type")
    private String type;

    @JsonProperty("body")
    private String body;

    @JsonProperty("date")
    private String date;
}