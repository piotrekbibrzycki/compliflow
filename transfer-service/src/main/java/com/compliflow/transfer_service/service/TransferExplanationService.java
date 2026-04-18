package com.compliflow.transfer_service.service;

import com.compliflow.transfer_service.dto.*;
import com.compliflow.transfer_service.model.AuditEvent;
import com.compliflow.transfer_service.model.ComplianceDecision;
import com.compliflow.transfer_service.model.ReviewDecision;
import com.compliflow.transfer_service.model.Transfer;
import com.compliflow.transfer_service.model.TransferStatus;
import com.compliflow.transfer_service.repository.AuditEventRepository;
import com.compliflow.transfer_service.repository.TransferRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransferExplanationService {

    private static final String ADMIN_REVIEW_RULE_NAME = "ADMIN_REVIEW";

    private final TransferRepository transferRepository;
    private final AuditEventRepository auditEventRepository;
    private final TransferAuditProofService transferAuditProofService;

    public TransferExplanationService(TransferRepository transferRepository,
                                      AuditEventRepository auditEventRepository,
                                      TransferAuditProofService transferAuditProofService) {
        this.transferRepository = transferRepository;
        this.auditEventRepository = auditEventRepository;
        this.transferAuditProofService = transferAuditProofService;
    }

    public TransferExplanationResponseDto getTransferExplanation(Long transferId) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Transfer not found: " + transferId));

        List<AuditEvent> auditEvents = auditEventRepository.findByTransferIdOrderByCreatedAtAsc(transferId);

        TransferExplanationReviewMetadataDto reviewMetadata = buildReviewMetadata(transfer, auditEvents);
        TransferExplanationStateFlagsDto stateFlags = buildStateFlags(transfer, auditEvents, reviewMetadata);

        return TransferExplanationResponseDto.builder()
                .transferId(transfer.getId())
                .transferStatus(transfer.getStatus())
                .complianceDecision(transfer.getComplianceDecision())
                .complianceSummaryReason(transfer.getComplianceReasonSummary())
                .auditEvents(auditEvents.stream()
                        .map(TransferExplanationAuditEventDto::fromEntity)
                        .toList())
                .finalExplanation(buildNarrative(transfer, auditEvents, reviewMetadata, stateFlags))
                .reviewMetadata(reviewMetadata)
                .stateFlags(stateFlags)
                .auditProof(transferAuditProofService.buildProofResponse(transfer, auditEvents))
                .build();
    }

    private TransferExplanationReviewMetadataDto buildReviewMetadata(Transfer transfer, List<AuditEvent> auditEvents) {
        AuditEvent adminReviewEvent = auditEvents.stream()
                .filter(event -> ADMIN_REVIEW_RULE_NAME.equals(event.getRuleName()))
                .reduce((first, second) -> second)
                .orElse(null);

        if (adminReviewEvent == null) {
            return TransferExplanationReviewMetadataDto.builder()
                    .reviewed(transfer.getReviewedAt() != null || transfer.getReviewedBy() != null)
                    .reviewDecision(null)
                    .reviewedBy(transfer.getReviewedBy())
                    .reviewedAt(transfer.getReviewedAt())
                    .reviewComment(transfer.getReviewComment())
                    .build();
        }

        return TransferExplanationReviewMetadataDto.builder()
                .reviewed(true)
                .reviewDecision(resolveReviewDecision(adminReviewEvent))
                .reviewedBy(transfer.getReviewedBy() != null ? transfer.getReviewedBy() : adminReviewEvent.getReviewedBy())
                .reviewedAt(transfer.getReviewedAt() != null ? transfer.getReviewedAt() : adminReviewEvent.getCreatedAt())
                .reviewComment(transfer.getReviewComment())
                .build();
    }

    private TransferExplanationStateFlagsDto buildStateFlags(
            Transfer transfer,
            List<AuditEvent> auditEvents,
            TransferExplanationReviewMetadataDto reviewMetadata
    ) {
        List<String> onChainTxHashes = auditEvents.stream()
                .map(AuditEvent::getOnChainTxHash)
                .filter(hash -> hash != null && !hash.isBlank())
                .distinct()
                .toList();

        boolean auditAnchoredOnChain = !onChainTxHashes.isEmpty();
        boolean auditAnchorVerified = auditEvents.stream()
                .anyMatch(event -> Boolean.TRUE.equals(event.getOnChainVerified()));

        return TransferExplanationStateFlagsDto.builder()
                .fundsMoved(didFundsMove(transfer))
                .reviewable(transfer.getStatus() == TransferStatus.PENDING_REVIEW)
                .blocked(transfer.getStatus() == TransferStatus.BLOCKED)
                .approved(reviewMetadata.getReviewDecision() == ReviewDecision.APPROVE)
                .rejected(reviewMetadata.getReviewDecision() == ReviewDecision.REJECT
                        || transfer.getStatus() == TransferStatus.REJECTED)
                .auditAnchoredOnChain(auditAnchoredOnChain)
                .auditAnchorVerified(auditAnchorVerified)
                .onChainTxHashes(onChainTxHashes)
                .build();
    }

    private TransferExplanationNarrativeDto buildNarrative(
            Transfer transfer,
            List<AuditEvent> auditEvents,
            TransferExplanationReviewMetadataDto reviewMetadata,
            TransferExplanationStateFlagsDto stateFlags
    ) {
        int flaggedRuleCount = (int) auditEvents.stream()
                .filter(event -> event.getDecision() == ComplianceDecision.FLAG || event.getDecision() == ComplianceDecision.BLOCK)
                .count();

        String summaryReason = defaultText(transfer.getComplianceReasonSummary(), "No compliance summary available.");
        String userExplanation = buildUserExplanation(transfer, summaryReason, stateFlags);
        String adminExplanation = buildAdminExplanation(transfer, summaryReason, flaggedRuleCount, reviewMetadata, stateFlags);

        return TransferExplanationNarrativeDto.builder()
                .userExplanation(userExplanation)
                .adminExplanation(adminExplanation)
                .build();
    }

    private String buildUserExplanation(
            Transfer transfer,
            String summaryReason,
            TransferExplanationStateFlagsDto stateFlags
    ) {
        return switch (transfer.getStatus()) {
            case COMPLETED -> {
                if (transfer.getComplianceDecision() == ComplianceDecision.FLAG) {
                    yield "Transfer was initially flagged for compliance review, then approved by an administrator, and funds were moved successfully. Main reason: "
                            + summaryReason;
                }
                yield "Transfer passed compliance checks and funds were moved successfully. Main reason: " + summaryReason;
            }
            case PENDING_REVIEW -> "Transfer was paused before any funds moved because compliance checks required manual review. Main reason: "
                    + summaryReason;
            case BLOCKED -> "Transfer was blocked before execution because compliance checks determined it should not proceed. Main reason: "
                    + summaryReason;
            case REJECTED -> "Transfer was reviewed manually and rejected. No funds were moved. Main reason: "
                    + summaryReason;
            case FAILED -> "Transfer was not completed because execution failed. Compliance decision was "
                    + transfer.getComplianceDecision() + ". Failure reason: "
                    + defaultText(transfer.getFailureReason(), summaryReason);
            case PENDING -> {
                if (stateFlags.isFundsMoved()) {
                    yield "Transfer is still marked as pending in storage, but funds appear to have moved. This indicates an inconsistent execution state that should be investigated.";
                }
                yield "Transfer is pending execution.";
            }
        };
    }

    private String buildAdminExplanation(
            Transfer transfer,
            String summaryReason,
            int flaggedRuleCount,
            TransferExplanationReviewMetadataDto reviewMetadata,
            TransferExplanationStateFlagsDto stateFlags
    ) {
        StringBuilder builder = new StringBuilder();

        builder.append("Transfer ")
                .append(transfer.getId())
                .append(" finished in status ")
                .append(transfer.getStatus())
                .append(" with compliance decision ")
                .append(transfer.getComplianceDecision())
                .append(". Summary reason: ")
                .append(summaryReason)
                .append('.');

        builder.append(" Funds moved: ")
                .append(stateFlags.isFundsMoved())
                .append('.');

        builder.append(" Reviewable: ")
                .append(stateFlags.isReviewable())
                .append('.');

        builder.append(" Triggered escalation events: ")
                .append(flaggedRuleCount)
                .append('.');

        if (reviewMetadata.isReviewed()) {
            builder.append(" Manual review decision: ")
                    .append(reviewMetadata.getReviewDecision())
                    .append(" by ")
                    .append(defaultText(reviewMetadata.getReviewedBy(), "unknown reviewer"));

            if (reviewMetadata.getReviewedAt() != null) {
                builder.append(" at ")
                        .append(reviewMetadata.getReviewedAt());
            }

            if (reviewMetadata.getReviewComment() != null && !reviewMetadata.getReviewComment().isBlank()) {
                builder.append(". Review comment: ")
                        .append(reviewMetadata.getReviewComment());
            }
            builder.append('.');
        }

        builder.append(" Audit anchored on-chain: ")
                .append(stateFlags.isAuditAnchoredOnChain())
                .append(". Anchor verified: ")
                .append(stateFlags.isAuditAnchorVerified())
                .append('.');

        return builder.toString();
    }

    private ReviewDecision resolveReviewDecision(AuditEvent adminReviewEvent) {
        if (adminReviewEvent.getDecision() == ComplianceDecision.PASS) {
            return ReviewDecision.APPROVE;
        }

        if (adminReviewEvent.getDecision() == ComplianceDecision.BLOCK) {
            return ReviewDecision.REJECT;
        }

        return null;
    }

    private boolean didFundsMove(Transfer transfer) {
        return transfer.getStatus() == TransferStatus.COMPLETED && transfer.getCompletedAt() != null;
    }

    private String defaultText(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }
}