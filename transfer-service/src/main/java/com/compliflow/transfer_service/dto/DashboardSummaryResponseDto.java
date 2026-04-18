package com.compliflow.transfer_service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryResponseDto {

    private long totalTransfers;
    private long completedTransfers;
    private long pendingReviewTransfers;
    private long blockedTransfers;
    private long rejectedTransfers;
    private long failedTransfers;
    private long flaggedTransfers;
    private long anchoredProofTransfers;
    private long walletRelatedTransfers;
}