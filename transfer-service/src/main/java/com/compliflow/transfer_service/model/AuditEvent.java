package com.compliflow.transfer_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long transferId;

    @Column(length = 34)
    private String sourceAccount;

    @Column(length = 128)
    private String targetReference;

    @Column(nullable = false, length = 100)
    private String ruleName;

    @Column(length = 100)
    private String policyCode;

    @Column(length = 50)
    private String policyVersion;

    @Enumerated(EnumType.STRING)
    @Column(length = 100)
    private PolicyScenarioCode scenarioCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplianceDecision decision;

    @Column(nullable = false, length = 512)
    private String reason;

    @Column(nullable = false, length = 255)
    private String legalContext;

    @Column(nullable = false, length = 255)
    private String internalPolicy;

    @Column(nullable = false, length = 1024)
    private String userFacingExplanation;

    @Column(length = 2048)
    private String metadataJson;

    @Column(length = 128)
    private String reviewedBy;

    @Column(length = 128)
    private String integrityHash;

    @Column(length = 128)
    private String onChainTxHash;

    private Boolean onChainVerified;

    @Column(length = 64)
    private String proofSchemaVersion;

    @Column(length = 64)
    private String anchorNetwork;

    private LocalDateTime anchoredAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();

        if (this.onChainVerified == null) {
            this.onChainVerified = false;
        }
    }
}