package com.compliflow.transfer_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fromAccount;

    @Column(nullable = false)
    private String toAccount;

    @Column(length = 128)
    private String targetWalletAddress;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ComplianceDecision complianceDecision = ComplianceDecision.PASS;

    @Column(length = 512)
    private String complianceReasonSummary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentRail paymentRail = PaymentRail.SEPA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CounterpartyType counterpartyType = CounterpartyType.BANK_ACCOUNT;

    @Column(length = 512)
    private String reviewComment;

    @Column(length = 100)
    private String reviewedBy;

    private LocalDateTime reviewedAt;

    private String failureReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();

        if (this.complianceDecision == null) {
            this.complianceDecision = ComplianceDecision.PASS;
        }
        if (this.paymentRail == null) {
            this.paymentRail = PaymentRail.SEPA;
        }
        if (this.counterpartyType == null) {
            this.counterpartyType = CounterpartyType.BANK_ACCOUNT;
        }
    }
}