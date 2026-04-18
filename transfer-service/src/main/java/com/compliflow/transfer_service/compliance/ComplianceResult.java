package com.compliflow.transfer_service.compliance;

import com.compliflow.transfer_service.model.ComplianceDecision;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ComplianceResult {

    private final ComplianceDecision finalDecision;
    private final List<RuleEvaluation> evaluations;
    private final String summaryReason;
    private final String userFacingExplanation;
    private final boolean reviewable;
    private final boolean appealable;
}