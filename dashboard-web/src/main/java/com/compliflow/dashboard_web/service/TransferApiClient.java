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

    public TransferExplanationResponse getTransferExplanation(DashboardSessionUser user, Long transferId) {
        try {
            return restClient.get()
                    .uri("/api/transfers/{id}/explanation", transferId)
                    .header(HttpHeaders.AUTHORIZATION, bearer(user))
                    .retrieve()
                    .body(TransferExplanationResponse.class);
        } catch (RestClientResponseException ex) {
            log.error("Transfer explanation call failed. transferId={}, status={}, body={}",
                    transferId, ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
            return null;
        } catch (Exception ex) {
            log.error("Transfer explanation call failed. transferId={}", transferId, ex);
            return null;
        }
    }

    private String bearer(DashboardSessionUser user) {
        return "Bearer " + user.getToken();
    }

    public static class TransferListResponse extends ArrayList<TransferSummaryItem> {
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
    public static class TransferExplanationResponse {
        private Long transferId;
        private String transferStatus;
        private String complianceDecision;
        private String complianceSummaryReason;
        private List<TransferExplanationAuditEvent> auditEvents;
        private TransferExplanationNarrative finalExplanation;
        private TransferExplanationReviewMetadata reviewMetadata;
        private TransferExplanationStateFlags stateFlags;
        private Object auditProof;
    }

    @Getter
    @Setter
    public static class TransferExplanationAuditEvent {
        private String ruleName;
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

    @Getter
    @Setter
    public static class TransferExplanationNarrative {
        private String userExplanation;
        private String adminExplanation;
    }

    @Getter
    @Setter
    public static class TransferExplanationReviewMetadata {
        private boolean reviewed;
        private String reviewDecision;
        private String reviewedBy;
        private LocalDateTime reviewedAt;
        private String reviewComment;
    }

    @Getter
    @Setter
    public static class TransferExplanationStateFlags {
        private boolean fundsMoved;
        private boolean reviewable;
        private boolean blocked;
        private boolean approved;
        private boolean rejected;
        private boolean auditAnchoredOnChain;
        private boolean auditAnchorVerified;
        private List<String> onChainTxHashes;
    }
}