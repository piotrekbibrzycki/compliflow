package com.compliflow.dashboard_web.service;

import com.compliflow.dashboard_web.config.DashboardApiProperties;
import com.compliflow.dashboard_web.session.DashboardSessionUser;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class TransferApiClient {

    private final RestClient restClient;

    public TransferApiClient(DashboardApiProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.getTransferServiceUrl())
                .build();
    }

    public List<TransferSummaryItem> getTransfers(DashboardSessionUser user) {
        try {
            TransferListResponse response = restClient.get()
                    .uri("/api/transfers")
                    .header(HttpHeaders.AUTHORIZATION, bearer(user))
                    .retrieve()
                    .body(TransferListResponse.class);

            return response == null ? List.of() : response;
        } catch (RestClientResponseException ex) {
            log.error("Transfers call failed. status={}, body={}",
                    ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
            return List.of();
        } catch (Exception ex) {
            log.error("Transfers call failed.", ex);
            return List.of();
        }
    }

    public List<TransferAuditEventItem> getTransferAuditEvents(DashboardSessionUser user, Long transferId) {
        try {
            AuditEventListResponse response = restClient.get()
                    .uri("/api/audit-events/transfer/{id}", transferId)
                    .header(HttpHeaders.AUTHORIZATION, bearer(user))
                    .retrieve()
                    .body(AuditEventListResponse.class);

            return response == null ? List.of() : response;
        } catch (RestClientResponseException ex) {
            log.error("Transfer audit events call failed. transferId={}, status={}, body={}",
                    transferId, ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
            return List.of();
        } catch (Exception ex) {
            log.error("Transfer audit events call failed. transferId={}", transferId, ex);
            return List.of();
        }
    }

    private String bearer(DashboardSessionUser user) {
        return "Bearer " + user.getToken();
    }

    public static class TransferListResponse extends ArrayList<TransferSummaryItem> {
    }

    public static class AuditEventListResponse extends ArrayList<TransferAuditEventItem> {
    }

    public void reviewTransfer(DashboardSessionUser user, Long transferId, String decision, String comment) {
        try {
            restClient.patch()
                    .uri("/api/transfers/{id}/review", transferId)
                    .header(HttpHeaders.AUTHORIZATION, bearer(user))
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body(new TransferReviewRequest(decision, comment))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            log.error("Transfer review call failed. transferId={}, status={}, body={}",
                    transferId, ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Transfer review call failed. transferId={}", transferId, ex);
            throw new IllegalStateException("Transfer review request failed", ex);
        }
    }

    public List<TransferSummaryItem> getPendingReviewTransfers(DashboardSessionUser user) {
        try {
            TransferListResponse response = restClient.get()
                    .uri("/api/transfers/pending-review")
                    .header(HttpHeaders.AUTHORIZATION, bearer(user))
                    .retrieve()
                    .body(TransferListResponse.class);

            return response == null ? List.of() : response;
        } catch (RestClientResponseException ex) {
            log.error("Pending review transfers call failed. status={}, body={}",
                    ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
            return List.of();
        } catch (Exception ex) {
            log.error("Pending review transfers call failed.", ex);
            return List.of();
        }
    }

    public List<ComplianceDecisionRuleItem> getDecisionRules(DashboardSessionUser user) {
        try {
            DecisionRuleListResponse response = restClient.get()
                    .uri("/api/compliance/decision-rules")
                    .header(HttpHeaders.AUTHORIZATION, bearer(user))
                    .retrieve()
                    .body(DecisionRuleListResponse.class);

            return response == null ? List.of() : response;
        } catch (RestClientResponseException ex) {
            log.error("Decision rules call failed. status={}, body={}",
                    ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
            return List.of();
        } catch (Exception ex) {
            log.error("Decision rules call failed.", ex);
            return List.of();
        }
    }
    public TransferProofItem getTransferProof(DashboardSessionUser user, Long transferId) {
        try {
            return restClient.get()
                    .uri("/api/transfers/{id}/proof", transferId)
                    .header(HttpHeaders.AUTHORIZATION, bearer(user))
                    .retrieve()
                    .body(TransferProofItem.class);
        } catch (RestClientResponseException ex) {
            log.error("Transfer proof call failed. transferId={}, status={}, body={}",
                    transferId, ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
            return null;
        } catch (Exception ex) {
            log.error("Transfer proof call failed. transferId={}", transferId, ex);
            return null;
        }
    }

    public void anchorTransferProof(DashboardSessionUser user, Long transferId) {
        try {
            restClient.post()
                    .uri("/api/transfers/{id}/proof/anchor", transferId)
                    .header(HttpHeaders.AUTHORIZATION, bearer(user))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            log.error("Transfer proof anchor call failed. transferId={}, status={}, body={}",
                    transferId, ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Transfer proof anchor call failed. transferId={}", transferId, ex);
            throw new IllegalStateException("Transfer proof anchor request failed", ex);
        }
    }

    public TransferProofVerificationItem verifyTransferProof(DashboardSessionUser user, Long transferId) {
        try {
            return restClient.get()
                    .uri("/api/transfers/{id}/proof/verify", transferId)
                    .header(HttpHeaders.AUTHORIZATION, bearer(user))
                    .retrieve()
                    .body(TransferProofVerificationItem.class);
        } catch (RestClientResponseException ex) {
            log.error("Transfer proof verify call failed. transferId={}, status={}, body={}",
                    transferId, ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Transfer proof verify call failed. transferId={}", transferId, ex);
            throw new IllegalStateException("Transfer proof verify request failed", ex);
        }
    }



    @Getter
    @Setter
    public static class TransferSummaryItem {
        private Long id;
        private String fromAccount;
        private String toAccount;
        private String targetWalletAddress;
        private BigDecimal amount;
        private String title;
        private String currency;
        private String status;
        private String complianceDecision;
        private String complianceReasonSummary;
        private String paymentRail;
        private String counterpartyType;
        private String reviewComment;
        private String reviewedBy;
        private LocalDateTime reviewedAt;
        private String failureReason;
        private LocalDateTime createdAt;
        private LocalDateTime completedAt;
    }

    @Getter
    @Setter
    public static class TransferAuditEventItem {
        private Long id;
        private Long transferId;
        private String sourceAccount;
        private String targetReference;
        private String ruleName;
        private String policyCode;
        private String policyVersion;
        private String scenarioCode;
        private String decision;
        private String reason;
        private String legalContext;
        private String internalPolicy;
        private String userFacingExplanation;
        private String metadataJson;
        private String reviewedBy;
        private String integrityHash;
        private String onChainTxHash;
        private Boolean onChainVerified;
        private String proofSchemaVersion;
        private String anchorNetwork;
        private LocalDateTime anchoredAt;
        private LocalDateTime createdAt;
    }

    public record TransferReviewRequest(
            String decision,
            String comment
    ) {
    }

    public static class DecisionRuleListResponse extends ArrayList<ComplianceDecisionRuleItem> {
    }

    @Getter
    @Setter
    public static class ComplianceDecisionRuleItem {
        private Long id;
        private Long decisionTableId;
        private String decisionTableCode;
        private String ruleCode;
        private String policyCode;
        private String policyVersion;
        private String scenarioCode;
        private Integer priority;
        private Boolean active;
        private java.math.BigDecimal minAmount;
        private java.math.BigDecimal maxAmount;
        private String currencyEquals;
        private String paymentRailEquals;
        private String counterpartyTypeEquals;
        private Integer sourceRiskScoreGte;
        private Integer destinationRiskScoreGte;
        private Integer recentTransfersLastHourGte;
        private Boolean sourceRestricted;
        private Boolean destinationRestricted;
        private Boolean targetWalletRestricted;
        private Boolean requiresTargetWallet;
        private String decision;
        private Boolean reviewAllowed;
        private String summaryReason;
        private String legalContext;
        private String internalPolicy;
        private String userFacingExplanation;
        private java.time.LocalDateTime createdAt;
    }
    @Getter
    @Setter
    public static class TransferProofItem {
        private Long transferId;
        private String transferStatus;
        private String proofSchemaVersion;
        private Integer auditEventCount;
        private Boolean anchored;
        private String storedIntegrityHash;
        private String currentComputedHash;
        private String onChainTxHash;
        private Boolean onChainVerified;
        private String anchorNetwork;
        private LocalDateTime anchoredAt;
        private String canonicalPayloadJson;
    }

    @Getter
    @Setter
    public static class TransferProofVerificationItem {
        private Long transferId;
        private String transferStatus;
        private String proofSchemaVersion;
        private String currentComputedHash;
        private String storedIntegrityHash;
        private Boolean matchesStoredHash;
        private Boolean anchoredOnChain;
        private Boolean contractLookupConfirmed;
        private String onChainTxHash;
        private String anchorNetwork;
        private LocalDateTime anchoredAt;
    }

}