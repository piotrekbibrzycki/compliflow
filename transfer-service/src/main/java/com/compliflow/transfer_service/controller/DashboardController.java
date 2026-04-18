package com.compliflow.transfer_service.controller;

import com.compliflow.transfer_service.dto.DashboardActivityItemDto;
import com.compliflow.transfer_service.dto.DashboardSummaryResponseDto;
import com.compliflow.transfer_service.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public DashboardSummaryResponseDto getSummary() {
        return dashboardService.getSummary();
    }

    @GetMapping("/activity")
    public List<DashboardActivityItemDto> getRecentActivity() {
        return dashboardService.getRecentActivity();
    }
}