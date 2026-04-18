package com.compliflow.transfer_service.compliance.rule;

import com.compliflow.transfer_service.compliance.ComplianceRule;
import com.compliflow.transfer_service.compliance.RuleEvaluation;
import com.compliflow.transfer_service.compliance.TransferContext;
import com.compliflow.transfer_service.model.ComplianceDecision;
import org.springframework.stereotype.Component;

@Component
public class RiskScoreRule implements ComplianceRule {

    private static final int HIGH_RISK_THRESHOLD = 70;

    @Override
    public RuleEvaluation evaluate(TransferContext context) {
        int sourceRiskScore = context.getSourceRiskScore() != null ? context.getSourceRiskScore() : 0;

        if (sourceRiskScore > HIGH_RISK_THRESHOLD) {
            return RuleEvaluation.builder()
                    .ruleName("RiskScoreRule")
                    .decision(ComplianceDecision.FLAG)
                    .reason("High-risk account requires manual review")
                    .legalContext("Risk-based enhanced monitoring")
                    .internalPolicy("Manual review required for accounts above risk score 70")
                    .userFacingExplanation("This transfer was flagged because the source account is classified as high risk.")
                    .metadataJson("{\"sourceRiskScore\":" + sourceRiskScore + "}")
                    .build();
        }

        return RuleEvaluation.pass(
                "RiskScoreRule",
                "Risk-based enhanced monitoring",
                "Manual review required for accounts above risk score 70",
                "Source account risk score is below the high-risk threshold."
        );
    }
}