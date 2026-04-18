package com.compliflow.transfer_service.compliance;

import com.compliflow.transfer_service.model.ComplianceDecision;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ComplianceService {

    private final List<ComplianceRule> rules;

    public ComplianceService(List<ComplianceRule> rules) {
        this.rules = rules;
    }

    public ComplianceResult evaluate(TransferContext context) {
        List<RuleEvaluation> evaluations = rules.stream()
                .map(rule -> rule.evaluate(context))
                .filter(Objects::nonNull)
                .toList();

        ComplianceDecision finalDecision = evaluations.stream()
                .map(RuleEvaluation::getDecision)
                .max(Comparator.comparingInt(this::decisionRank))
                .orElse(ComplianceDecision.PASS);

        String summaryReason = evaluations.stream()
                .filter(e -> e.getDecision() != ComplianceDecision.PASS)
                .map(e -> e.getRuleName() + ": " + e.getReason())
                .collect(Collectors.joining(" | "));

        if (summaryReason.isBlank()) {
            summaryReason = "No compliance concerns detected";
        }

        String userFacingExplanation = evaluations.stream()
                .filter(e -> e.getDecision() != ComplianceDecision.PASS)
                .map(RuleEvaluation::getUserFacingExplanation)
                .collect(Collectors.joining(" "));

        if (userFacingExplanation.isBlank()) {
            userFacingExplanation = "This transfer passed all compliance checks.";
        }

        return ComplianceResult.builder()
                .finalDecision(finalDecision)
                .evaluations(evaluations)
                .summaryReason(summaryReason)
                .userFacingExplanation(userFacingExplanation)
                .reviewable(finalDecision == ComplianceDecision.FLAG)
                .appealable(finalDecision == ComplianceDecision.BLOCK)
                .build();
    }

    private int decisionRank(ComplianceDecision decision) {
        return switch (decision) {
            case PASS -> 1;
            case FLAG -> 2;
            case BLOCK -> 3;
        };
    }
}