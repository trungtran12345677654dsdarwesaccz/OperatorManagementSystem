package org.example.operatormanagementsystem.managestaff_yen.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

    @Data
    @AllArgsConstructor
    public class TopOperatorResponse {
        private Integer operatorId;
        private String operatorName;
        private int successOrders;
        private double onTimeRate;
    }