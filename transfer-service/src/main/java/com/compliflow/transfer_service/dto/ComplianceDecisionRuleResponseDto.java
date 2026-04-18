package com.compliflow.transfer_service.dto;

import com.compliflow.transfer_service.model.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceDecisionRuleResponseDto {

    private Long id;
    private Long decisionTableId;
    private String decisionTableCode;
    private String ruleCode;
    private String policyCode;
    private String policyVersion;
    private PolicyScenarioCode scenarioCode;
    private Integer priority;
    private Boolean active;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String currencyEquals;
    private PaymentRail paymentRailEquals;
    private CounterpartyType counterpartyTypeEquals;
    private Integer sourceRiskScoreGte;
    private Integer destinationRiskScoreGte;
    private Integer recentTransfersLastHourGte;
    private Boolean sourceRestricted;
    private Boolean destinationRestricted;
    private Boolean targetWalletRestricted;
    private Boolean requiresTargetWallet;
    private ComplianceDecision decision;
    private Boolean reviewAllowed;
    private String summaryReason;
    private String legalContext;
    private String internalPolicy;
    private String userFacingExplanation;
    private LocalDateTime createdAt;

    public static ComplianceDecisionRuleResponseDto fromEntity(ComplianceDecisionRule rule) {
        return ComplianceDecisionRuleResponseDto.builder()
                .id(rule.getId())
                .decisionTableId(rule.getDecisionTable() != null ? rule.getDecisionTable().getId() : null)
                .decisionTableCode(rule.getDecisionTable() != null ? rule.getDecisionTable().getTableCode() : null)
                .ruleCode(rule.getRuleCode())
                .policyCode(rule.getPolicyCode())
                .policyVersion(rule.getPolicyVersion())
                .scenarioCode(rule.getScenarioCode())
                .priority(rule.getPriority())
                .active(rule.getActive())
                .minAmount(rule.getMinAmount())
                .maxAmount(rule.getMaxAmount())
                .currencyEquals(rule.getCurrencyEquals())
                .paymentRailEquals(rule.getPaymentRailEquals())
                .counterpartyTypeEquals(rule.getCounterpartyTypeEquals())
                .sourceRiskScoreGte(rule.getSourceRiskScoreGte())
                .destinationRiskScoreGte(rule.getDestinationRiskScoreGte())
                .recentTransfersLastHourGte(rule.getRecentTransfersLastHourGte())
                .sourceRestricted(rule.getSourceRestricted())
                .destinationRestricted(rule.getDestinationRestricted())
                .targetWalletRestricted(rule.getTargetWalletRestricted())
                .requiresTargetWallet(rule.getRequiresTargetWallet())
                .decision(rule.getDecision())
                .reviewAllowed(rule.getReviewAllowed())
                .summaryReason(rule.getSummaryReason())
                .legalContext(rule.getLegalContext())
                .internalPolicy(rule.getInternalPolicy())
                .userFacingExplanation(rule.getUserFacingExplanation())
                .createdAt(rule.getCreatedAt())
                .build();
    }
}