package com.compliflow.transfer_service.compliance.rule;

import com.compliflow.transfer_service.compliance.RuleEvaluation;
import com.compliflow.transfer_service.compliance.TransferContext;
import com.compliflow.transfer_service.model.ComplianceDecision;
import com.compliflow.transfer_service.model.CounterpartyType;
import com.compliflow.transfer_service.model.PaymentRail;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ThresholdRuleTest {

    private final ThresholdRule rule = new ThresholdRule();

    @Test
    void shouldFlagWhenAmountExceedsThreshold() {
        TransferContext context = baseContextBuilder()
                .amount(new BigDecimal("15000.00"))
                .build();

        RuleEvaluation result = rule.evaluate(context);

        assertEquals(ComplianceDecision.FLAG, result.getDecision());
        assertEquals("ThresholdRule", result.getRuleName());
    }

    @Test
    void shouldPassWhenAmountIsWithinThreshold() {
        TransferContext context = baseContextBuilder()
                .amount(new BigDecimal("5000.00"))
                .build();

        RuleEvaluation result = rule.evaluate(context);

        assertEquals(ComplianceDecision.PASS, result.getDecision());
    }

    private TransferContext.TransferContextBuilder baseContextBuilder() {
        return TransferContext.builder()
                .sourceAccountNumber("ACC-001")
                .destinationAccountNumber("ACC-002")
                .amount(new BigDecimal("100.00"))
                .currency("PLN")
                .title("Test")
                .paymentRail(PaymentRail.SEPA)
                .counterpartyType(CounterpartyType.BANK_ACCOUNT)
                .sourceRiskScore(10)
                .destinationRiskScore(5)
                .recentTransfersLastHour(1)
                .sourceRestricted(false)
                .destinationRestricted(false)
                .targetWalletRestricted(false);
    }
}