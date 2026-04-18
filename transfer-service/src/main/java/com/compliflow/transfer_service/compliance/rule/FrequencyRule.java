package com.compliflow.transfer_service.compliance.rule;

import com.compliflow.transfer_service.compliance.ComplianceRule;
import com.compliflow.transfer_service.compliance.RuleEvaluation;
import com.compliflow.transfer_service.compliance.TransferContext;
import com.compliflow.transfer_service.model.ComplianceDecision;
import org.springframework.stereotype.Component;

@Component
public class FrequencyRule implements ComplianceRule {

    private static final int MAX_TRANSFERS_PER_HOUR = 5;

    @Override
    public RuleEvaluation evaluate(TransferContext context) {
        if (context.getRecentTransfersLastHour() > MAX_TRANSFERS_PER_HOUR) {
            return RuleEvaluation.builder()
                    .ruleName("FrequencyRule")
                    .decision(ComplianceDecision.FLAG)
                    .reason("Unusual transfer frequency detected")
                    .legalContext("Monitoring for unusual transaction patterns")
                    .internalPolicy("Flag more than 5 transfers from same account within 1 hour")
                    .userFacingExplanation("This transfer was flagged because the account has unusually frequent transfer activity.")
                    .metadataJson("{\"recentTransfersLastHour\":" + context.getRecentTransfersLastHour() + "}")
                    .build();
        }

        return RuleEvaluation.pass(
                "FrequencyRule",
                "Monitoring for unusual transaction patterns",
                "Flag more than 5 transfers from same account within 1 hour",
                "Recent transfer frequency is within normal limits."
        );
    }
}