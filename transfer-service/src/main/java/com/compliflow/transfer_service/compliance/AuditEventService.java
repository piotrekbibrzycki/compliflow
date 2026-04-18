package com.compliflow.transfer_service.compliance;

import com.compliflow.transfer_service.model.AuditEvent;
import com.compliflow.transfer_service.model.ComplianceDecision;
import com.compliflow.transfer_service.model.ReviewDecision;
import com.compliflow.transfer_service.model.Transfer;
import com.compliflow.transfer_service.repository.AuditEventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditEventService {

    private final AuditEventRepository auditEventRepository;

    public AuditEventService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    public List<AuditEvent> getAllAuditEvents() {
        return auditEventRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<AuditEvent> getAuditEventsByTransferId(Long transferId) {
        return auditEventRepository.findByTransferIdOrderByCreatedAtDesc(transferId);
    }

    public void saveEvaluations(Transfer transfer, List<RuleEvaluation> evaluations) {
        List<AuditEvent> events = evaluations.stream()
                .map(evaluation -> AuditEvent.builder()
                        .transferId(transfer.getId())
                        .sourceAccount(transfer.getFromAccount())
                        .targetReference(resolveTargetReference(transfer))
                        .ruleName(evaluation.getRuleName())
                        .policyCode(evaluation.getPolicyCode())
                        .policyVersion(evaluation.getPolicyVersion())
                        .scenarioCode(evaluation.getScenarioCode())
                        .decision(evaluation.getDecision())
                        .reason(evaluation.getReason())
                        .legalContext(evaluation.getLegalContext())
                        .internalPolicy(evaluation.getInternalPolicy())
                        .userFacingExplanation(evaluation.getUserFacingExplanation())
                        .metadataJson(evaluation.getMetadataJson())
                        .reviewedBy(null)
                        .integrityHash(null)
                        .onChainTxHash(null)
                        .onChainVerified(false)
                        .build())
                .toList();

        auditEventRepository.saveAll(events);
    }

    public void saveAdminReviewDecision(
            Transfer transfer,
            ReviewDecision reviewDecision,
            String reviewedBy,
            String comment
    ) {
        AuditEvent event = AuditEvent.builder()
                .transferId(transfer.getId())
                .sourceAccount(transfer.getFromAccount())
                .targetReference(resolveTargetReference(transfer))
                .ruleName("ADMIN_REVIEW")
                .policyCode("MANUAL_REVIEW")
                .policyVersion("1.0")
                .scenarioCode(null)
                .decision(reviewDecision == ReviewDecision.APPROVE ? ComplianceDecision.PASS : ComplianceDecision.BLOCK)
                .reason(reviewDecision == ReviewDecision.APPROVE
                        ? "Flagged transfer approved by administrator"
                        : "Flagged transfer rejected by administrator")
                .legalContext("Manual compliance review decision")
                .internalPolicy("Flagged transfers require explicit administrative decision")
                .userFacingExplanation(reviewDecision == ReviewDecision.APPROVE
                        ? "This transfer was approved after manual compliance review."
                        : "This transfer was rejected after manual compliance review.")
                .metadataJson("{\"reviewDecision\":\"" + reviewDecision + "\",\"comment\":\"" + safeJson(comment) + "\"}")
                .reviewedBy(reviewedBy)
                .integrityHash(null)
                .onChainTxHash(null)
                .onChainVerified(false)
                .build();

        auditEventRepository.save(event);
    }

    private String safeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\"", "\\\"");
    }

    private String resolveTargetReference(Transfer transfer) {
        if (transfer.getTargetWalletAddress() != null && !transfer.getTargetWalletAddress().isBlank()) {
            return transfer.getTargetWalletAddress();
        }
        return transfer.getToAccount();
    }
}