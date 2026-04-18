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
}