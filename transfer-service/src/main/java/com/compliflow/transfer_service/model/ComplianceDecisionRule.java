package com.compliflow.transfer_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "compliance_decision_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceDecisionRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "decision_table_id", nullable = false)
    private ComplianceDecisionTable decisionTable;

    @Column(nullable = false, length = 100)
    private String ruleCode;

    @Column(nullable = false, length = 100)
    private String policyCode;

    @Column(nullable = false, length = 50)
    private String policyVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private PolicyScenarioCode scenarioCode;

    @Column(nullable = false)
    private Integer priority;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(precision = 19, scale = 2)
    private BigDecimal minAmount;

    @Column(precision = 19, scale = 2)
    private BigDecimal maxAmount;

    @Column(length = 3)
    private String currencyEquals;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentRail paymentRailEquals;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private CounterpartyType counterpartyTypeEquals;

    private Integer sourceRiskScoreGte;

    private Integer destinationRiskScoreGte;

    private Integer recentTransfersLastHourGte;

    private Boolean sourceRestricted;

    private Boolean destinationRestricted;

    private Boolean targetWalletRestricted;

    private Boolean requiresTargetWallet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ComplianceDecision decision;

    @Column(nullable = false)
    @Builder.Default
    private Boolean reviewAllowed = false;

    @Column(nullable = false, length = 512)
    private String summaryReason;

    @Column(nullable = false, length = 255)
    private String legalContext;

    @Column(nullable = false, length = 255)
    private String internalPolicy;

    @Column(nullable = false, length = 1024)
    private String userFacingExplanation;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();

        if (this.active == null) {
            this.active = true;
        }
        if (this.reviewAllowed == null) {
            this.reviewAllowed = false;
        }
    }
}