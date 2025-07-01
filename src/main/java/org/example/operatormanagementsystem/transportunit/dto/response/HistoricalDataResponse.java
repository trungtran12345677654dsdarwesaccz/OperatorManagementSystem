package org.example.operatormanagementsystem.transportunit.dto.response;

public interface HistoricalDataResponse {
    String getPeriod();
    int getPending();
    int getActive();
    int getInactive();
    int getTotalApprovals();
    int getTotalRejections();
}
