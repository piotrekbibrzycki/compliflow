package com.compliflow.transfer_service.compliance;

import com.compliflow.transfer_service.compliance.rule.FrequencyRule;
import com.compliflow.transfer_service.compliance.rule.RiskScoreRule;
import com.compliflow.transfer_service.compliance.rule.ThresholdRule;
import com.compliflow.transfer_service.compliance.rule.WatchlistRule;
import com.compliflow.transfer_service.compliance.rule.WalletScreeningRule;
import com.compliflow.transfer_service.model.ComplianceDecision;
import com.compliflow.transfer_service.model.CounterpartyType;
import com.compliflow.transfer_service.model.PaymentRail;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComplianceServiceTest {

    private final ComplianceService complianceService = new ComplianceService(List.of(
            new ThresholdRule(),
            new FrequencyRule(),
            new RiskScoreRule(),
            new WatchlistRule(),
            new WalletScreeningRule()
    ));

    @Test
    void shouldReturnPassWhenAllRulesPass() {
        TransferContext context = baseContextBuilder().build();

        ComplianceResult result = complianceService.evaluate(context);

        assertEquals(ComplianceDecision.PASS, result.getFinalDecision());
        assertFalse(result.isReviewable());
        assertFalse(result.isAppealable());
        assertEquals(5, result.getEvaluations().size());
    }

    @Test
    void shouldReturnFlagWhenOnlyFlagRulesTrigger() {
        TransferContext context = baseContextBuilder()
                .amount(new BigDecimal("20000.00"))
                .sourceRiskScore(80)
                .build();

        ComplianceResult result = complianceService.evaluate(context);

        assertEquals(ComplianceDecision.FLAG, result.getFinalDecision());
        assertTrue(result.isReviewable());
        assertFalse(result.isAppealable());
    }

    @Test
    void shouldReturnBlockWhenAnyBlockRuleTriggers() {
        TransferContext context = baseContextBuilder()
                .amount(new BigDecimal("20000.00"))
                .sourceRestricted(true)
                .build();

        ComplianceResult result = complianceService.evaluate(context);

        assertEquals(ComplianceDecision.BLOCK, result.getFinalDecision());
        assertFalse(result.isReviewable());
        assertTrue(result.isAppealable());
    }

    @Test
    void shouldPreferBlockOverFlag() {
        TransferContext context = baseContextBuilder()
                .recentTransfersLastHour(8)
                .sourceRestricted(true)
                .build();

        ComplianceResult result = complianceService.evaluate(context);

        assertEquals(ComplianceDecision.BLOCK, result.getFinalDecision());
    }

    private TransferContext.TransferContextBuilder baseContextBuilder() {
        return TransferContext.builder()
                .sourceAccountNumber("ACC-001")
                .destinationAccountNumber("ACC-002")
                .targetWalletAddress(null)
                .amount(new BigDecimal("100.00"))
                .currency("PLN")
                .title("Test transfer")
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