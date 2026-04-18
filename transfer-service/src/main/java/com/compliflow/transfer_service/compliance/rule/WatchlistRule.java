package com.compliflow.transfer_service.compliance.rule;

import com.compliflow.transfer_service.compliance.ComplianceRule;
import com.compliflow.transfer_service.compliance.RuleEvaluation;
import com.compliflow.transfer_service.compliance.TransferContext;
import com.compliflow.transfer_service.model.ComplianceDecision;
import org.springframework.stereotype.Component;

@Component
public class WatchlistRule implements ComplianceRule {

    @Override
    public RuleEvaluation evaluate(TransferContext context) {
        if (context.isSourceRestricted() || context.isDestinationRestricted()) {
            return RuleEvaluation.builder()
                    .ruleName("WatchlistRule")
                    .decision(ComplianceDecision.BLOCK)
                    .reason("Source or destination account matched restricted-party dataset")
                    .legalContext("Restricted-party / sanctions screening")
                    .internalPolicy("Block transactions involving restricted counterparties")
                    .userFacingExplanation("This transfer was blocked because one of the counterparties matched a restricted-party dataset.")
                    .metadataJson("{\"sourceRestricted\":" + context.isSourceRestricted()
                            + ",\"destinationRestricted\":" + context.isDestinationRestricted() + "}")
                    .build();
        }

        return RuleEvaluation.pass(
                "WatchlistRule",
                "Restricted-party / sanctions screening",
                "Block transactions involving restricted counterparties",
                "No restricted-party match was found for the source or destination account."
        );
    }
}