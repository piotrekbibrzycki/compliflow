package com.compliflow.transfer_service.dto;

import lombok.*;

import java.util.Map;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceSummaryReportResponseDto {

    private Map<String, Long> decisionBreakdown;
    private Map<String, Long> statusBreakdown;
    private long totalAuditEvents;
    private long totalAnchoredProofs;
    private List<ComplianceSummaryPolicyMetricDto> topPolicyHits;
}