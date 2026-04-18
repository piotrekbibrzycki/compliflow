package com.compliflow.transfer_service.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferExplanationStateFlagsDto {

    private boolean fundsMoved;
    private boolean reviewable;
    private boolean blocked;
    private boolean approved;
    private boolean rejected;
    private boolean auditAnchoredOnChain;
    private boolean auditAnchorVerified;
    private List<String> onChainTxHashes;
}