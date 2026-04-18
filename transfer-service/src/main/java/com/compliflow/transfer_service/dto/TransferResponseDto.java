package com.compliflow.transfer_service.dto;

import com.compliflow.transfer_service.model.ComplianceDecision;
import com.compliflow.transfer_service.model.CounterpartyType;
import com.compliflow.transfer_service.model.PaymentRail;
import com.compliflow.transfer_service.model.Transfer;
import com.compliflow.transfer_service.model.TransferStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferResponseDto {

    private Long id;
    private String fromAccount;
    private String toAccount;
    private String targetWalletAddress;
    private BigDecimal amount;
    private String title;
    private String currency;
    private TransferStatus status;
    private ComplianceDecision complianceDecision;
    private String complianceReasonSummary;
    private PaymentRail paymentRail;
    private CounterpartyType counterpartyType;
    private String reviewComment;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public static TransferResponseDto toDto(Transfer transfer) {
        return TransferResponseDto.builder()
                .id(transfer.getId())
                .fromAccount(transfer.getFromAccount())
                .toAccount(transfer.getToAccount())
                .targetWalletAddress(transfer.getTargetWalletAddress())
                .amount(transfer.getAmount())
                .title(transfer.getTitle())
                .currency(transfer.getCurrency())
                .status(transfer.getStatus())
                .complianceDecision(transfer.getComplianceDecision())
                .complianceReasonSummary(transfer.getComplianceReasonSummary())
                .paymentRail(transfer.getPaymentRail())
                .counterpartyType(transfer.getCounterpartyType())
                .reviewComment(transfer.getReviewComment())
                .reviewedBy(transfer.getReviewedBy())
                .reviewedAt(transfer.getReviewedAt())
                .failureReason(transfer.getFailureReason())
                .createdAt(transfer.getCreatedAt())
                .completedAt(transfer.getCompletedAt())
                .build();
    }
}