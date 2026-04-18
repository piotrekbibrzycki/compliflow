package com.compliflow.transfer_service.compliance;

import com.compliflow.transfer_service.model.ComplianceDecision;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RuleEvaluation {

    private final String ruleName;
    private final ComplianceDecision decision;
    private final String reason;
    private final String legalContext;
    private final String internalPolicy;
    private final String userFacingExplanation;
    private final String metadataJson;

    public static RuleEvaluation pass(
            String ruleName,
            String legalContext,
            String internalPolicy,
            String userFacingExplanation
    ) {
        return RuleEvaluation.builder()
                .ruleName(ruleName)
                .decision(ComplianceDecision.PASS)
                .reason("Rule passed")
                .legalContext(legalContext)
                .internalPolicy(internalPolicy)
                .userFacingExplanation(userFacingExplanation)
                .metadataJson(null)
                .build();
    }
}