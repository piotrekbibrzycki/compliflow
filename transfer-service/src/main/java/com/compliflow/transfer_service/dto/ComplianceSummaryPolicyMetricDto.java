package com.compliflow.transfer_service.dto;

import com.compliflow.transfer_service.model.ComplianceDecision;
import com.compliflow.transfer_service.model.PolicyScenarioCode;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceSummaryPolicyMetricDto {

    private String policyCode;
    private String policyVersion;
    private PolicyScenarioCode scenarioCode;
    private ComplianceDecision decision;
    private long hitCount;
}