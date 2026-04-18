package com.compliflow.transfer_service.controller;

import com.compliflow.transfer_service.dto.ComplianceSummaryReportResponseDto;
import com.compliflow.transfer_service.service.ReportingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportingController {

    private final ReportingService reportingService;

    public ReportingController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/compliance-summary")
    public ComplianceSummaryReportResponseDto getComplianceSummary() {
        return reportingService.getComplianceSummary();
    }
}