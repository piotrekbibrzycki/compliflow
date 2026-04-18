package com.compliflow.transfer_service.dto;

import com.compliflow.transfer_service.model.TransferStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferAuditProofVerificationResponseDto {

    private Long transferId;
    private TransferStatus transferStatus;
    private String proofSchemaVersion;
    private String currentComputedHash;
    private String storedIntegrityHash;
    private boolean matchesStoredHash;
    private boolean anchoredOnChain;
    private boolean contractLookupConfirmed;
    private String onChainTxHash;
    private String anchorNetwork;
    private LocalDateTime anchoredAt;
}