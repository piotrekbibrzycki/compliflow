package com.compliflow.transfer_service.dto;

import com.compliflow.transfer_service.model.ComplianceDecision;
import com.compliflow.transfer_service.model.TransferStatus;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferExplanationResponseDto {

    private Long transferId;
    private TransferStatus transferStatus;
    private ComplianceDecision complianceDecision;
    private String complianceSummaryReason;
    private List<TransferExplanationAuditEventDto> auditEvents;
    private TransferExplanationNarrativeDto finalExplanation;
    private TransferExplanationReviewMetadataDto reviewMetadata;
    private TransferExplanationStateFlagsDto stateFlags;
}