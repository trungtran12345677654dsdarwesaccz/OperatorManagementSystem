package org.example.operatormanagementsystem.dashboardstaff.dto.response;

import lombok.Getter;
import lombok.Setter;

<<<<<<< HEAD
=======
import java.math.BigDecimal;

>>>>>>> origin/phong
@Getter
@Setter
public class DetailDataResponse {
    private String month;
    private String unit;
    private int trips;
    private String revenue;
    private int onTime;
    private int cancelled;
    private int late;
}