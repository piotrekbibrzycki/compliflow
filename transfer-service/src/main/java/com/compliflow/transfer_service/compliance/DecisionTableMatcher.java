package com.compliflow.transfer_service.compliance;

import com.compliflow.transfer_service.model.ComplianceDecisionRule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DecisionTableMatcher {

    public boolean matches(ComplianceDecisionRule rule, TransferContext context) {
        if (!matchesMinAmount(rule.getMinAmount(), context.getAmount())) {
            return false;
        }
        if (!matchesMaxAmount(rule.getMaxAmount(), context.getAmount())) {
            return false;
        }
        if (!matchesCurrency(rule.getCurrencyEquals(), context.getCurrency())) {
            return false;
        }
        if (!matchesPaymentRail(rule, context)) {
            return false;
        }
        if (!matchesCounterpartyType(rule, context)) {
            return false;
        }
        if (!matchesSourceRiskScore(rule.getSourceRiskScoreGte(), context.getSourceRiskScore())) {
            return false;
        }
        if (!matchesDestinationRiskScore(rule.getDestinationRiskScoreGte(), context.getDestinationRiskScore())) {
            return false;
        }
        if (!matchesRecentTransfers(rule.getRecentTransfersLastHourGte(), context.getRecentTransfersLastHour())) {
            return false;
        }
        if (!matchesBoolean(rule.getSourceRestricted(), context.isSourceRestricted())) {
            return false;
        }
        if (!matchesBoolean(rule.getDestinationRestricted(), context.isDestinationRestricted())) {
            return false;
        }
        if (!matchesBoolean(rule.getTargetWalletRestricted(), context.isTargetWalletRestricted())) {
            return false;
        }
        return matchesTargetWalletPresence(rule.getRequiresTargetWallet(), context.getTargetWalletAddress());
    }

    private boolean matchesMinAmount(BigDecimal minAmount, BigDecimal amount) {
        return minAmount == null || (amount != null && amount.compareTo(minAmount) >= 0);
    }

    private boolean matchesMaxAmount(BigDecimal maxAmount, BigDecimal amount) {
        return maxAmount == null || (amount != null && amount.compareTo(maxAmount) <= 0);
    }

    private boolean matchesCurrency(String expectedCurrency, String actualCurrency) {
        return expectedCurrency == null
                || (actualCurrency != null && expectedCurrency.equalsIgnoreCase(actualCurrency));
    }

    private boolean matchesPaymentRail(ComplianceDecisionRule rule, TransferContext context) {
        return rule.getPaymentRailEquals() == null || rule.getPaymentRailEquals() == context.getPaymentRail();
    }

    private boolean matchesCounterpartyType(ComplianceDecisionRule rule, TransferContext context) {
        return rule.getCounterpartyTypeEquals() == null || rule.getCounterpartyTypeEquals() == context.getCounterpartyType();
    }

    private boolean matchesSourceRiskScore(Integer threshold, Integer actualRiskScore) {
        return threshold == null || (actualRiskScore != null && actualRiskScore >= threshold);
    }

    private boolean matchesDestinationRiskScore(Integer threshold, Integer actualRiskScore) {
        return threshold == null || (actualRiskScore != null && actualRiskScore >= threshold);
    }

    private boolean matchesRecentTransfers(Integer threshold, int actualRecentTransfers) {
        return threshold == null || actualRecentTransfers >= threshold;
    }

    private boolean matchesBoolean(Boolean expected, boolean actual) {
        return expected == null || expected == actual;
    }

    private boolean matchesTargetWalletPresence(Boolean requiresTargetWallet, String targetWalletAddress) {
        if (requiresTargetWallet == null) {
            return true;
        }

        boolean hasTargetWallet = targetWalletAddress != null && !targetWalletAddress.isBlank();
        return requiresTargetWallet == hasTargetWallet;
    }
}