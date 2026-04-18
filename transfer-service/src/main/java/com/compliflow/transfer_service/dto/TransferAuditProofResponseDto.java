package com.compliflow.transfer_service.dto;

import com.compliflow.transfer_service.model.TransferStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferAuditProofResponseDto {

    private Long transferId;
    private TransferStatus transferStatus;
    private String proofSchemaVersion;
    private Integer auditEventCount;
    private boolean anchored;
    private String storedIntegrityHash;
    private String currentComputedHash;
    private String onChainTxHash;
    private Boolean onChainVerified;
    private String anchorNetwork;
    private LocalDateTime anchoredAt;
    private String canonicalPayloadJson;
}