package org.example.operatormanagementsystem.payment.utils;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "vietqr")
public class VietQrProperties {
    private String bankId;
    private String accountNumber;
    private String accountName;
}
