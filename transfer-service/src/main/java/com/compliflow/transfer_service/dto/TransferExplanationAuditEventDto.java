package com.compliflow.transfer_service.dto;

import com.compliflow.transfer_service.model.AuditEvent;
import com.compliflow.transfer_service.model.ComplianceDecision;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferExplanationAuditEventDto {

    private String ruleName;
    private ComplianceDecision decision;
    private String reason;
    private String legalContext;
    private String internalPolicy;
    private String userFacingExplanation;
    private String metadataJson;
    private String reviewedBy;
    private String integrityHash;
    private String onChainTxHash;
    private Boolean onChainVerified;
    private LocalDateTime createdAt;

    public static TransferExplanationAuditEventDto fromEntity(AuditEvent auditEvent) {
        return TransferExplanationAuditEventDto.builder()
                .ruleName(auditEvent.getRuleName())
                .decision(auditEvent.getDecision())
                .reason(auditEvent.getReason())
                .legalContext(auditEvent.getLegalContext())
                .internalPolicy(auditEvent.getInternalPolicy())
                .userFacingExplanation(auditEvent.getUserFacingExplanation())
                .metadataJson(auditEvent.getMetadataJson())
                .reviewedBy(auditEvent.getReviewedBy())
                .integrityHash(auditEvent.getIntegrityHash())
                .onChainTxHash(auditEvent.getOnChainTxHash())
                .onChainVerified(auditEvent.getOnChainVerified())
                .createdAt(auditEvent.getCreatedAt())
                .build();
    }
}