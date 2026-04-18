package com.compliflow.transfer_service.compliance;

import com.compliflow.transfer_service.model.AuditEvent;
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

    public void saveEvaluations(Transfer transfer, List<RuleEvaluation> evaluations) {
        List<AuditEvent> events = evaluations.stream()
                .map(evaluation -> AuditEvent.builder()
                        .transferId(transfer.getId())
                        .sourceAccount(transfer.getFromAccount())
                        .targetReference(resolveTargetReference(transfer))
                        .ruleName(evaluation.getRuleName())
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

    private String resolveTargetReference(Transfer transfer) {
        if (transfer.getTargetWalletAddress() != null && !transfer.getTargetWalletAddress().isBlank()) {
            return transfer.getTargetWalletAddress();
        }
        return transfer.getToAccount();
    }
}