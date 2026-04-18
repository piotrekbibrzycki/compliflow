package com.compliflow.dashboard_web.service;

import com.compliflow.dashboard_web.config.DashboardApiProperties;
import com.compliflow.dashboard_web.session.DashboardSessionUser;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TransferApiClient {

    private final RestClient restClient;

    public TransferApiClient(DashboardApiProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.getTransferServiceUrl())
                .build();
    }

    public DashboardSummaryResponse getDashboardSummary(DashboardSessionUser user) {
        return restClient.get()
                .uri("/api/dashboard/summary")
                .header(HttpHeaders.AUTHORIZATION, bearer(user))
                .retrieve()
                .body(DashboardSummaryResponse.class);
    }

    public List<DashboardActivityItem> getDashboardActivity(DashboardSessionUser user) {
        ActivityListResponse response = restClient.get()
                .uri("/api/dashboard/activity")
                .header(HttpHeaders.AUTHORIZATION, bearer(user))
                .retrieve()
                .body(ActivityListResponse.class);

        return response == null ? List.of() : response;
    }

    public ComplianceSummaryReport getComplianceSummaryReport(DashboardSessionUser user) {
        return restClient.get()
                .uri("/api/reports/compliance-summary")
                .header(HttpHeaders.AUTHORIZATION, bearer(user))
                .retrieve()
                .body(ComplianceSummaryReport.class);
    }

    private String bearer(DashboardSessionUser user) {
        return "Bearer " + user.getToken();
    }

    public static class ActivityListResponse extends ArrayList<DashboardActivityItem> {
    }

    @Getter
    @Setter
    public static class DashboardSummaryResponse {
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

    @Getter
    @Setter
    public static class DashboardActivityItem {
        private String activityType;
        private String title;
        private String description;
        private Long transferId;
        private String severity;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    public static class ComplianceSummaryReport {
        private Map<String, Long> decisionBreakdown;
        private Map<String, Long> statusBreakdown;
        private long totalAuditEvents;
        private long totalAnchoredProofs;
        private List<ComplianceSummaryPolicyMetric> topPolicyHits;
    }

    @Getter
    @Setter
    public static class ComplianceSummaryPolicyMetric {
        private String policyCode;
        private String policyVersion;
        private String scenarioCode;
        private String decision;
        private long hitCount;
    }
}