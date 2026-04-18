package com.compliflow.transfer_service.compliance.rule;

import com.compliflow.transfer_service.compliance.ComplianceRule;
import com.compliflow.transfer_service.compliance.RuleEvaluation;
import com.compliflow.transfer_service.compliance.TransferContext;
import com.compliflow.transfer_service.model.ComplianceDecision;
import com.compliflow.transfer_service.model.CounterpartyType;
import org.springframework.stereotype.Component;

@Component
public class WalletScreeningRule implements ComplianceRule {

    @Override
    public RuleEvaluation evaluate(TransferContext context) {
        if (context.getCounterpartyType() == CounterpartyType.WALLET_ADDRESS && context.isTargetWalletRestricted()) {
            return RuleEvaluation.builder()
                    .ruleName("WalletScreeningRule")
                    .decision(ComplianceDecision.BLOCK)
                    .reason("Target wallet address matched restricted-party dataset")
                    .legalContext("Restricted-party screening for wallet-based payouts")
                    .internalPolicy("Block wallet payouts to restricted wallet addresses")
                    .userFacingExplanation("This transfer was blocked because the destination wallet address matched a restricted-party dataset.")
                    .metadataJson("{\"targetWalletAddress\":\"" + context.getTargetWalletAddress() + "\"}")
                    .build();
        }

        return RuleEvaluation.pass(
                "WalletScreeningRule",
                "Restricted-party screening for wallet-based payouts",
                "Block wallet payouts to restricted wallet addresses",
                "Wallet screening passed or transfer is not a wallet-based payout."
        );
    }
}