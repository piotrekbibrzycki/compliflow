package com.compliflow.transfer_service.compliance;

public interface ComplianceRule {
    RuleEvaluation evaluate(TransferContext context);
}
