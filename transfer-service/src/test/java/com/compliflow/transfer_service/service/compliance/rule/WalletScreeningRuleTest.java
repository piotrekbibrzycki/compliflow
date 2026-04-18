package com.compliflow.transfer_service.compliance.rule;

import com.compliflow.transfer_service.compliance.RuleEvaluation;
import com.compliflow.transfer_service.compliance.TransferContext;
import com.compliflow.transfer_service.model.ComplianceDecision;
import com.compliflow.transfer_service.model.CounterpartyType;
import com.compliflow.transfer_service.model.PaymentRail;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WalletScreeningRuleTest {

    private final WalletScreeningRule rule = new WalletScreeningRule();

    @Test
    void shouldBlockWhenWalletTransferTargetsRestrictedWallet() {
        TransferContext context = baseContextBuilder()
                .counterpartyType(CounterpartyType.WALLET_ADDRESS)
                .targetWalletAddress("0xabc123")
                .targetWalletRestricted(true)
                .build();

        RuleEvaluation result = rule.evaluate(context);

        assertEquals(ComplianceDecision.BLOCK, result.getDecision());
    }

    @Test
    void shouldPassWhenWalletTransferTargetsSafeWallet() {
        TransferContext context = baseContextBuilder()
                .counterpartyType(CounterpartyType.WALLET_ADDRESS)
                .targetWalletAddress("0xsafe123")
                .targetWalletRestricted(false)
                .build();

        RuleEvaluation result = rule.evaluate(context);

        assertEquals(ComplianceDecision.PASS, result.getDecision());
    }

    @Test
    void shouldPassForNonWalletTransfers() {
        TransferContext context = baseContextBuilder()
                .counterpartyType(CounterpartyType.BANK_ACCOUNT)
                .targetWalletRestricted(true)
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