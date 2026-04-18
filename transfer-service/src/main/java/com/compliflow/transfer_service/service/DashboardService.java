package com.compliflow.transfer_service.service;

import com.compliflow.transfer_service.dto.DashboardActivityItemDto;
import com.compliflow.transfer_service.dto.DashboardSummaryResponseDto;
import com.compliflow.transfer_service.model.AuditEvent;
import com.compliflow.transfer_service.model.ComplianceDecision;
import com.compliflow.transfer_service.model.CounterpartyType;
import com.compliflow.transfer_service.model.ReviewDecision;
import com.compliflow.transfer_service.model.Transfer;
import com.compliflow.transfer_service.model.TransferStatus;
import com.compliflow.transfer_service.repository.AuditEventRepository;
import com.compliflow.transfer_service.repository.TransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DashboardService {

    private final TransferRepository transferRepository;
    private final AuditEventRepository auditEventRepository;

    public DashboardService(TransferRepository transferRepository, AuditEventRepository auditEventRepository) {
        this.transferRepository = transferRepository;
        this.auditEventRepository = auditEventRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponseDto getSummary() {
        return DashboardSummaryResponseDto.builder()
                .totalTransfers(transferRepository.count())
                .completedTransfers(transferRepository.countByStatus(TransferStatus.COMPLETED))
                .pendingReviewTransfers(transferRepository.countByStatus(TransferStatus.PENDING_REVIEW))
                .blockedTransfers(transferRepository.countByStatus(TransferStatus.BLOCKED))
                .rejectedTransfers(transferRepository.countByStatus(TransferStatus.REJECTED))
                .failedTransfers(transferRepository.countByStatus(TransferStatus.FAILED))
                .flaggedTransfers(transferRepository.countByComplianceDecision(ComplianceDecision.FLAG))
                .anchoredProofTransfers(auditEventRepository.countDistinctAnchoredTransferIds())
                .walletRelatedTransfers(transferRepository.countByCounterpartyType(CounterpartyType.WALLET_ADDRESS))
                .build();
    }

    @Transactional(readOnly = true)
    public List<DashboardActivityItemDto> getRecentActivity() {
        List<DashboardActivityItemDto> items = new ArrayList<>();

        List<Transfer> recentTransfers = transferRepository.findTop20ByOrderByCreatedAtDesc();
        recentTransfers.forEach(transfer -> {
            items.add(buildTransferCreatedActivity(transfer));

            DashboardActivityItemDto outcomeActivity = buildTransferOutcomeActivity(transfer);
            if (outcomeActivity != null) {
                items.add(outcomeActivity);
            }
        });

        List<AuditEvent> recentAuditEvents = auditEventRepository.findTop50ByOrderByCreatedAtDesc();
        Set<String> seenProofAnchors = new HashSet<>();

        recentAuditEvents.forEach(auditEvent -> {
            DashboardActivityItemDto reviewActivity = buildManualReviewActivity(auditEvent);
            if (reviewActivity != null) {
                items.add(reviewActivity);
            }

            DashboardActivityItemDto proofActivity = buildProofAnchoredActivity(auditEvent, seenProofAnchors);
            if (proofActivity != null) {
                items.add(proofActivity);
            }
        });

        return items.stream()
                .filter(item -> item.getCreatedAt() != null)
                .sorted(Comparator.comparing(DashboardActivityItemDto::getCreatedAt).reversed())
                .limit(25)
                .toList();
    }

    private DashboardActivityItemDto buildTransferCreatedActivity(Transfer transfer) {
        return DashboardActivityItemDto.builder()
                .activityType("TRANSFER_CREATED")
                .title("Transfer created")
                .description("Transfer " + transfer.getId() + " was created with compliance decision " + transfer.getComplianceDecision())
                .transferId(transfer.getId())
                .severity(severityForDecision(transfer.getComplianceDecision()))
                .createdAt(transfer.getCreatedAt())
                .build();
    }

    private DashboardActivityItemDto buildTransferOutcomeActivity(Transfer transfer) {
        if (transfer.getStatus() == TransferStatus.COMPLETED && transfer.getCompletedAt() != null) {
            return DashboardActivityItemDto.builder()
                    .activityType("TRANSFER_COMPLETED")
                    .title("Transfer completed")
                    .description("Transfer " + transfer.getId() + " completed successfully")
                    .transferId(transfer.getId())
                    .severity("success")
                    .createdAt(transfer.getCompletedAt())
                    .build();
        }

        if (transfer.getStatus() == TransferStatus.BLOCKED) {
            return DashboardActivityItemDto.builder()
                    .activityType("TRANSFER_BLOCKED")
                    .title("Transfer blocked")
                    .description("Transfer " + transfer.getId() + " was blocked by compliance")
                    .transferId(transfer.getId())
                    .severity("critical")
                    .createdAt(transfer.getCreatedAt())
                    .build();
        }

        if (transfer.getStatus() == TransferStatus.REJECTED && transfer.getReviewedAt() != null) {
            return DashboardActivityItemDto.builder()
                    .activityType("TRANSFER_REJECTED")
                    .title("Transfer rejected")
                    .description("Transfer " + transfer.getId() + " was rejected during manual review")
                    .transferId(transfer.getId())
                    .severity("critical")
                    .createdAt(transfer.getReviewedAt())
                    .build();
        }

        if (transfer.getStatus() == TransferStatus.FAILED) {
            return DashboardActivityItemDto.builder()
                    .activityType("TRANSFER_FAILED")
                    .title("Transfer failed")
                    .description("Transfer " + transfer.getId() + " failed: " + defaultText(transfer.getFailureReason(), "No failure reason provided"))
                    .transferId(transfer.getId())
                    .severity("warning")
                    .createdAt(transfer.getCompletedAt() != null ? transfer.getCompletedAt() : transfer.getCreatedAt())
                    .build();
        }

        return null;
    }

    private DashboardActivityItemDto buildManualReviewActivity(AuditEvent auditEvent) {
        if (!"ADMIN_REVIEW".equals(auditEvent.getRuleName())) {
            return null;
        }

        ReviewDecision reviewDecision = resolveReviewDecision(auditEvent);
        String title = reviewDecision == ReviewDecision.APPROVE
                ? "Manual review approved"
                : "Manual review rejected";

        String description = title + " for transfer " + auditEvent.getTransferId()
                + " by " + defaultText(auditEvent.getReviewedBy(), "unknown reviewer");

        return DashboardActivityItemDto.builder()
                .activityType("MANUAL_REVIEW")
                .title(title)
                .description(description)
                .transferId(auditEvent.getTransferId())
                .severity(reviewDecision == ReviewDecision.APPROVE ? "success" : "critical")
                .createdAt(auditEvent.getCreatedAt())
                .build();
    }

    private DashboardActivityItemDto buildProofAnchoredActivity(AuditEvent auditEvent, Set<String> seenProofAnchors) {
        if (auditEvent.getOnChainTxHash() == null || auditEvent.getOnChainTxHash().isBlank()) {
            return null;
        }

        String uniqueKey = auditEvent.getTransferId() + "::" + auditEvent.getOnChainTxHash();
        if (!seenProofAnchors.add(uniqueKey)) {
            return null;
        }

        return DashboardActivityItemDto.builder()
                .activityType("PROOF_ANCHORED")
                .title("Blockchain proof anchored")
                .description("Transfer " + auditEvent.getTransferId() + " audit proof was anchored on-chain")
                .transferId(auditEvent.getTransferId())
                .severity(Boolean.TRUE.equals(auditEvent.getOnChainVerified()) ? "success" : "info")
                .createdAt(auditEvent.getCreatedAt())
                .build();
    }

    private ReviewDecision resolveReviewDecision(AuditEvent auditEvent) {
        if (auditEvent.getDecision() == ComplianceDecision.PASS) {
            return ReviewDecision.APPROVE;
        }
        return ReviewDecision.REJECT;
    }

    private String severityForDecision(ComplianceDecision decision) {
        return switch (decision) {
            case PASS -> "success";
            case FLAG -> "warning";
            case BLOCK -> "critical";
        };
    }

    private String defaultText(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }
}