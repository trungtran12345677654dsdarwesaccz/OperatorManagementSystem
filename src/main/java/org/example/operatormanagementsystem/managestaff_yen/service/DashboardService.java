package org.example.operatormanagementsystem.managestaff_yen.service;

import org.example.operatormanagementsystem.managestaff_yen.dto.request.ChartFilterRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.request.TopOperatorFilterRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.response.*;

import java.util.List;

public interface DashboardService {

    DashboardOverviewResponse getOverview();

    List<RecentIssueResponse> getRecentIssues(int limit);

    List<TopOperatorResponse> getTopOperators(TopOperatorFilterRequest request);

    List<ChartDataPointResponse> getChartData(ChartFilterRequest request);
}