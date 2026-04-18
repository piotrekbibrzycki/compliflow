package com.compliflow.transfer_service.service;

import com.compliflow.transfer_service.compliance.AuditEventService;
import com.compliflow.transfer_service.dto.TransferAuditProofResponseDto;
import com.compliflow.transfer_service.dto.TransferAuditProofVerificationResponseDto;
import com.compliflow.transfer_service.model.AuditEvent;
import com.compliflow.transfer_service.model.Transfer;
import com.compliflow.transfer_service.model.TransferStatus;
import com.compliflow.transfer_service.repository.AuditEventRepository;
import com.compliflow.transfer_service.repository.TransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class TransferAuditProofService {

    private static final Set<TransferStatus> FINAL_STATUSES = Set.of(
            TransferStatus.COMPLETED,
            TransferStatus.BLOCKED,
            TransferStatus.REJECTED,
            TransferStatus.FAILED
    );

    private final TransferRepository transferRepository;
    private final AuditEventRepository auditEventRepository;
    private final ComplianceProofPayloadBuilder complianceProofPayloadBuilder;
    private final ProofHashService proofHashService;
    private final BlockchainAnchorService blockchainAnchorService;
    private final AuditEventService auditEventService;

    public TransferAuditProofService(
            TransferRepository transferRepository,
            AuditEventRepository auditEventRepository,
            ComplianceProofPayloadBuilder complianceProofPayloadBuilder,
            ProofHashService proofHashService,
            BlockchainAnchorService blockchainAnchorService,
            AuditEventService auditEventService
    ) {
        this.transferRepository = transferRepository;
        this.auditEventRepository = auditEventRepository;
        this.complianceProofPayloadBuilder = complianceProofPayloadBuilder;
        this.proofHashService = proofHashService;
        this.blockchainAnchorService = blockchainAnchorService;
        this.auditEventService = auditEventService;
    }

    @Transactional
    public TransferAuditProofResponseDto anchorTransferProof(Long transferId) {
        Transfer transfer = getTransfer(transferId);
        ensureFinalStatus(transfer);

        List<AuditEvent> auditEvents = getOrderedAuditEvents(transferId);
        if (auditEvents.isEmpty()) {
            throw new IllegalStateException("Cannot anchor proof for transfer without audit events: " + transferId);
        }

        String canonicalPayloadJson = complianceProofPayloadBuilder.buildCanonicalPayload(transfer, auditEvents);
        String computedHash = proofHashService.computeIntegrityHash(canonicalPayloadJson);
        StoredProofSnapshot snapshot = resolveStoredProofSnapshot(auditEvents);

        if (snapshot.hasAnyProofData()) {
            if (snapshot.integrityHash() == null || !snapshot.integrityHash().equals(computedHash)) {
                throw new IllegalStateException("Stored proof state is inconsistent with the current computed compliance proof for transfer: " + transferId);
            }

            return buildResponse(transfer, auditEvents, canonicalPayloadJson, computedHash, snapshot);
        }

        BlockchainAnchorService.AnchorResult anchorResult = blockchainAnchorService.anchorProofHash(
                computedHash,
                transferId,
                complianceProofPayloadBuilder.getProofSchemaVersion()
        );

        auditEventService.attachBlockchainProofToTransferEvents(
                transferId,
                computedHash,
                anchorResult.transactionHash(),
                anchorResult.verified(),
                complianceProofPayloadBuilder.getProofSchemaVersion(),
                anchorResult.network(),
                anchorResult.anchoredAt()
        );

        List<AuditEvent> refreshedAuditEvents = getOrderedAuditEvents(transferId);
        StoredProofSnapshot refreshedSnapshot = resolveStoredProofSnapshot(refreshedAuditEvents);

        return buildResponse(transfer, refreshedAuditEvents, canonicalPayloadJson, computedHash, refreshedSnapshot);
    }

    @Transactional(readOnly = true)
    public TransferAuditProofResponseDto getTransferProof(Long transferId) {
        Transfer transfer = getTransfer(transferId);
        List<AuditEvent> auditEvents = getOrderedAuditEvents(transferId);
        return buildProofResponse(transfer, auditEvents);
    }

    @Transactional(readOnly = true)
    public TransferAuditProofVerificationResponseDto verifyTransferProof(Long transferId) {
        Transfer transfer = getTransfer(transferId);
        List<AuditEvent> auditEvents = getOrderedAuditEvents(transferId);

        if (auditEvents.isEmpty()) {
            throw new IllegalStateException("Cannot verify proof for transfer without audit events: " + transferId);
        }

        String canonicalPayloadJson = complianceProofPayloadBuilder.buildCanonicalPayload(transfer, auditEvents);
        String computedHash = proofHashService.computeIntegrityHash(canonicalPayloadJson);
        StoredProofSnapshot snapshot = resolveStoredProofSnapshot(auditEvents);

        boolean matchesStoredHash = snapshot.integrityHash() != null
                && snapshot.integrityHash().equals(computedHash);

        boolean contractLookupConfirmed = false;
        if (matchesStoredHash && snapshot.integrityHash() != null && snapshot.onChainTxHash() != null && !snapshot.onChainTxHash().isBlank()) {
            try {
                contractLookupConfirmed = blockchainAnchorService.isProofAnchored(snapshot.integrityHash());
            } catch (IllegalStateException ex) {
                contractLookupConfirmed = Boolean.TRUE.equals(snapshot.onChainVerified());
            }
        }

        return TransferAuditProofVerificationResponseDto.builder()
                .transferId(transfer.getId())
                .transferStatus(transfer.getStatus())
                .proofSchemaVersion(snapshot.proofSchemaVersion() != null
                        ? snapshot.proofSchemaVersion()
                        : complianceProofPayloadBuilder.getProofSchemaVersion())
                .currentComputedHash(computedHash)
                .storedIntegrityHash(snapshot.integrityHash())
                .matchesStoredHash(matchesStoredHash)
                .anchoredOnChain(snapshot.onChainTxHash() != null && !snapshot.onChainTxHash().isBlank())
                .contractLookupConfirmed(contractLookupConfirmed)
                .onChainTxHash(snapshot.onChainTxHash())
                .anchorNetwork(snapshot.anchorNetwork())
                .anchoredAt(snapshot.anchoredAt())
                .build();
    }

    TransferAuditProofResponseDto buildProofResponse(Transfer transfer, List<AuditEvent> auditEvents) {
        if (auditEvents.isEmpty()) {
            return TransferAuditProofResponseDto.builder()
                    .transferId(transfer.getId())
                    .transferStatus(transfer.getStatus())
                    .proofSchemaVersion(complianceProofPayloadBuilder.getProofSchemaVersion())
                    .auditEventCount(0)
                    .anchored(false)
                    .storedIntegrityHash(null)
                    .currentComputedHash(null)
                    .onChainTxHash(null)
                    .onChainVerified(false)
                    .anchorNetwork(null)
                    .anchoredAt(null)
                    .canonicalPayloadJson(null)
                    .build();
        }

        String canonicalPayloadJson = complianceProofPayloadBuilder.buildCanonicalPayload(transfer, auditEvents);
        String computedHash = proofHashService.computeIntegrityHash(canonicalPayloadJson);
        StoredProofSnapshot snapshot = resolveStoredProofSnapshot(auditEvents);

        return buildResponse(transfer, auditEvents, canonicalPayloadJson, computedHash, snapshot);
    }

    private TransferAuditProofResponseDto buildResponse(
            Transfer transfer,
            List<AuditEvent> auditEvents,
            String canonicalPayloadJson,
            String computedHash,
            StoredProofSnapshot snapshot
    ) {
        return TransferAuditProofResponseDto.builder()
                .transferId(transfer.getId())
                .transferStatus(transfer.getStatus())
                .proofSchemaVersion(snapshot.proofSchemaVersion() != null
                        ? snapshot.proofSchemaVersion()
                        : complianceProofPayloadBuilder.getProofSchemaVersion())
                .auditEventCount(auditEvents.size())
                .anchored(snapshot.onChainTxHash() != null && !snapshot.onChainTxHash().isBlank())
                .storedIntegrityHash(snapshot.integrityHash())
                .currentComputedHash(computedHash)
                .onChainTxHash(snapshot.onChainTxHash())
                .onChainVerified(snapshot.onChainVerified())
                .anchorNetwork(snapshot.anchorNetwork())
                .anchoredAt(snapshot.anchoredAt())
                .canonicalPayloadJson(canonicalPayloadJson)
                .build();
    }

    private Transfer getTransfer(Long transferId) {
        return transferRepository.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Transfer not found: " + transferId));
    }

    private List<AuditEvent> getOrderedAuditEvents(Long transferId) {
        return auditEventRepository.findByTransferIdOrderByCreatedAtAsc(transferId);
    }

    private void ensureFinalStatus(Transfer transfer) {
        if (!FINAL_STATUSES.contains(transfer.getStatus())) {
            throw new IllegalStateException("Transfer must be in a final state before blockchain anchoring. Current status: " + transfer.getStatus());
        }
    }

    private StoredProofSnapshot resolveStoredProofSnapshot(List<AuditEvent> auditEvents) {
        String integrityHash = uniqueOrNull(auditEvents.stream().map(AuditEvent::getIntegrityHash).toList(), "integrityHash");
        String onChainTxHash = uniqueOrNull(auditEvents.stream().map(AuditEvent::getOnChainTxHash).toList(), "onChainTxHash");
        Boolean onChainVerified = uniqueOrNull(auditEvents.stream().map(AuditEvent::getOnChainVerified).toList(), "onChainVerified");
        String proofSchemaVersion = uniqueOrNull(auditEvents.stream().map(AuditEvent::getProofSchemaVersion).toList(), "proofSchemaVersion");
        String anchorNetwork = uniqueOrNull(auditEvents.stream().map(AuditEvent::getAnchorNetwork).toList(), "anchorNetwork");
        LocalDateTime anchoredAt = uniqueOrNull(auditEvents.stream().map(AuditEvent::getAnchoredAt).toList(), "anchoredAt");

        return new StoredProofSnapshot(
                integrityHash,
                onChainTxHash,
                onChainVerified,
                proofSchemaVersion,
                anchorNetwork,
                anchoredAt
        );
    }

    private <T> T uniqueOrNull(List<T> values, String fieldName) {
        List<T> distinctValues = values.stream()
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.comparing(Object::toString))
                .toList();

        if (distinctValues.size() > 1) {
            throw new IllegalStateException("Inconsistent stored blockchain proof field across audit events: " + fieldName);
        }

        return distinctValues.isEmpty() ? null : distinctValues.getFirst();
    }

    private record StoredProofSnapshot(
            String integrityHash,
            String onChainTxHash,
            Boolean onChainVerified,
            String proofSchemaVersion,
            String anchorNetwork,
            LocalDateTime anchoredAt
    ) {
        private boolean hasAnyProofData() {
            return integrityHash != null
                    || onChainTxHash != null
                    || Boolean.TRUE.equals(onChainVerified)
                    || proofSchemaVersion != null
                    || anchorNetwork != null
                    || anchoredAt != null;
        }
    }
}