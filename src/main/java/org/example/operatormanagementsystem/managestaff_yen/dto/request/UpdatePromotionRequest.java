package org.example.operatormanagementsystem.managestaff_yen.dto.request;

import org.example.operatormanagementsystem.enumeration.DiscountType;
import org.example.operatormanagementsystem.enumeration.PromotionStatus;

import java.util.Date;

public class UpdatePromotionRequest {
    private Long id;
    private String name;
    private String description;
    private Date startDate;
    private Date endDate;
    private PromotionStatus status;
    private DiscountType discountType;   // <<== THÊM
    private Double discountValue;        // <<== THÊM

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public PromotionStatus getStatus() { return status; }
    public void setStatus(PromotionStatus status) { this.status = status; }

    public DiscountType getDiscountType() { return discountType; }
    public void setDiscountType(DiscountType discountType) { this.discountType = discountType; }

    public Double getDiscountValue() { return discountValue; }
    public void setDiscountValue(Double discountValue) { this.discountValue = discountValue; }
}
