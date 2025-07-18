package org.example.operatormanagementsystem.dashboardstaff.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransportDataResponse {
    private int totalShipments;
    private String revenue;
    private double deliveryRate;
    private double totalVolume;
    private double shipmentGrowth;
    private double revenueGrowth;
    private double deliveryRateGrowth;
    private double volumeGrowth;
}