package com.compliflow.transfer_service.compliance.rule;

import com.compliflow.transfer_service.compliance.ComplianceRule;
import com.compliflow.transfer_service.compliance.RuleEvaluation;
import com.compliflow.transfer_service.compliance.TransferContext;
import com.compliflow.transfer_service.model.ComplianceDecision;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ThresholdRule implements ComplianceRule {

    private static final BigDecimal THRESHOLD = new BigDecimal("10000.00");

    @Override
    public RuleEvaluation evaluate(TransferContext context) {
        if (context.getAmount() != null && context.getAmount().compareTo(THRESHOLD) > 0) {
            return RuleEvaluation.builder()
                    .ruleName("ThresholdRule")
                    .decision(ComplianceDecision.FLAG)
                    .reason("Transfer exceeds internal enhanced review threshold")
                    .legalContext("AML risk-based transaction monitoring")
                    .internalPolicy("Flag transfers above 10,000.00")
                    .userFacingExplanation("This transfer was flagged because it exceeds the internal enhanced review threshold.")
                    .metadataJson("{\"threshold\":\"10000.00\"}")
                    .build();
        }

        return RuleEvaluation.pass(
                "ThresholdRule",
                "AML risk-based transaction monitoring",
                "Flag transfers above 10,000.00",
                "Transfer amount does not exceed the enhanced review threshold."
        );
    }
}