package com.compliflow.transfer_service.compliance;

import com.compliflow.transfer_service.model.ComplianceDecision;
import com.compliflow.transfer_service.model.ComplianceDecisionRule;
import com.compliflow.transfer_service.repository.ComplianceDecisionRuleRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DecisionTableComplianceService {

    private final ComplianceDecisionRuleRepository complianceDecisionRuleRepository;
    private final DecisionTableMatcher decisionTableMatcher;

    public DecisionTableComplianceService(
            ComplianceDecisionRuleRepository complianceDecisionRuleRepository,
            DecisionTableMatcher decisionTableMatcher
    ) {
        this.complianceDecisionRuleRepository = complianceDecisionRuleRepository;
        this.decisionTableMatcher = decisionTableMatcher;
    }

    public ComplianceResult evaluate(TransferContext context) {
        List<ComplianceDecisionRule> activeRules = complianceDecisionRuleRepository
                .findByDecisionTableActiveTrueAndActiveTrueOrderByPriorityAscIdAsc();

        List<RuleEvaluation> evaluations = activeRules.stream()
                .filter(rule -> decisionTableMatcher.matches(rule, context))
                .map(rule -> toEvaluation(rule, context))
                .toList();

        if (evaluations.isEmpty()) {
            return ComplianceResult.builder()
                    .finalDecision(ComplianceDecision.PASS)
                    .evaluations(List.of(defaultPassEvaluation()))
                    .summaryReason("No compliance concerns detected")
                    .userFacingExplanation("This transfer passed all compliance checks.")
                    .reviewable(false)
                    .appealable(false)
                    .build();
        }

        ComplianceDecision finalDecision = evaluations.stream()
                .map(RuleEvaluation::getDecision)
                .max(Comparator.comparingInt(this::decisionRank))
                .orElse(ComplianceDecision.PASS);

        String summaryReason = evaluations.stream()
                .map(evaluation -> evaluation.getPolicyCode() + ": " + evaluation.getReason())
                .collect(Collectors.joining(" | "));

        String userFacingExplanation = evaluations.stream()
                .map(RuleEvaluation::getUserFacingExplanation)
                .distinct()
                .collect(Collectors.joining(" "));

        boolean reviewable = finalDecision == ComplianceDecision.FLAG && evaluations.stream()
                .anyMatch(RuleEvaluation::isReviewAllowed);

        return ComplianceResult.builder()
                .finalDecision(finalDecision)
                .evaluations(evaluations)
                .summaryReason(summaryReason)
                .userFacingExplanation(userFacingExplanation)
                .reviewable(reviewable)
                .appealable(finalDecision == ComplianceDecision.BLOCK)
                .build();
    }

    private RuleEvaluation toEvaluation(ComplianceDecisionRule rule, TransferContext context) {
        return RuleEvaluation.builder()
                .ruleName(rule.getRuleCode())
                .policyCode(rule.getPolicyCode())
                .policyVersion(rule.getPolicyVersion())
                .scenarioCode(rule.getScenarioCode())
                .decision(rule.getDecision())
                .reason(rule.getSummaryReason())
                .legalContext(rule.getLegalContext())
                .internalPolicy(rule.getInternalPolicy())
                .userFacingExplanation(rule.getUserFacingExplanation())
                .metadataJson(buildMetadataJson(rule, context))
                .reviewAllowed(Boolean.TRUE.equals(rule.getReviewAllowed()))
                .build();
    }

    private RuleEvaluation defaultPassEvaluation() {
        return RuleEvaluation.builder()
                .ruleName("DEFAULT_PASS")
                .policyCode("TRANSFER_COMPLIANCE_DEFAULT_PASS")
                .policyVersion("1.0")
                .scenarioCode(null)
                .decision(ComplianceDecision.PASS)
                .reason("No compliance concerns detected")
                .legalContext("Baseline transaction screening")
                .internalPolicy("Allow execution when no active compliance policy row matches")
                .userFacingExplanation("This transfer passed all compliance checks.")
                .metadataJson(null)
                .reviewAllowed(false)
                .build();
    }

    private String buildMetadataJson(ComplianceDecisionRule rule, TransferContext context) {
        String targetWalletAddress = context.getTargetWalletAddress() == null
                ? ""
                : context.getTargetWalletAddress().replace("\"", "\\\"");

        return "{" +
                "\"tableCode\":\"" + escape(rule.getDecisionTable().getTableCode()) + "\"," +
                "\"scenarioCode\":\"" + rule.getScenarioCode() + "\"," +
                "\"amount\":" + context.getAmount() + "," +
                "\"currency\":\"" + escape(context.getCurrency()) + "\"," +
                "\"paymentRail\":\"" + context.getPaymentRail() + "\"," +
                "\"counterpartyType\":\"" + context.getCounterpartyType() + "\"," +
                "\"sourceRiskScore\":" + context.getSourceRiskScore() + "," +
                "\"destinationRiskScore\":" + context.getDestinationRiskScore() + "," +
                "\"recentTransfersLastHour\":" + context.getRecentTransfersLastHour() + "," +
                "\"sourceRestricted\":" + context.isSourceRestricted() + "," +
                "\"destinationRestricted\":" + context.isDestinationRestricted() + "," +
                "\"targetWalletRestricted\":" + context.isTargetWalletRestricted() + "," +
                "\"targetWalletAddress\":\"" + targetWalletAddress + "\"" +
                "}";
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\"", "\\\"");
    }

    private int decisionRank(ComplianceDecision decision) {
        return switch (decision) {
            case PASS -> 1;
            case FLAG -> 2;
            case BLOCK -> 3;
        };
    }
}