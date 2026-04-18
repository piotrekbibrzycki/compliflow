package com.compliflow.dashboard_web.controller;

import com.compliflow.dashboard_web.service.TransferApiClient;
import com.compliflow.dashboard_web.service.TransferApiClient.TransferSummaryItem;
import com.compliflow.dashboard_web.session.DashboardSessionService;
import com.compliflow.dashboard_web.session.DashboardSessionUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardPageController {

    private final DashboardSessionService dashboardSessionService;
    private final TransferApiClient transferApiClient;

    public DashboardPageController(DashboardSessionService dashboardSessionService,
                                   TransferApiClient transferApiClient) {
        this.dashboardSessionService = dashboardSessionService;
        this.transferApiClient = transferApiClient;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model, HttpSession session) {
        DashboardSessionUser user = dashboardSessionService.getUser(session);
        List<TransferSummaryItem> transfers = transferApiClient.getTransfers(user);

        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("pageSubtitle", "Compliance operations overview");
        model.addAttribute("currentUserEmail", user.getEmail());

        model.addAttribute("summary", buildSummary(transfers));
        model.addAttribute("activity", buildActivity(transfers));
        model.addAttribute("report", buildReport(transfers));

        return "dashboard/index";
    }

    private DashboardSummaryVm buildSummary(List<TransferSummaryItem> transfers) {
        DashboardSummaryVm vm = new DashboardSummaryVm();
        vm.setTotalTransfers(transfers.size());
        vm.setCompletedTransfers(countByStatus(transfers, "COMPLETED"));
        vm.setPendingReviewTransfers(countByStatus(transfers, "PENDING_REVIEW"));
        vm.setBlockedTransfers(countByStatus(transfers, "BLOCKED"));
        vm.setRejectedTransfers(countByStatus(transfers, "REJECTED"));
        vm.setFailedTransfers(countByStatus(transfers, "FAILED"));
        vm.setFlaggedTransfers(countByDecision(transfers, "FLAG"));
        vm.setAnchoredProofTransfers(0L);
        vm.setWalletRelatedTransfers(countByCounterpartyType(transfers, "WALLET_ADDRESS"));
        return vm;
    }

    private List<ActivityItemVm> buildActivity(List<TransferSummaryItem> transfers) {
        return transfers.stream()
                .sorted(Comparator.comparing(TransferSummaryItem::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(12)
                .map(this::toActivityItem)
                .toList();
    }

    private ComplianceReportVm buildReport(List<TransferSummaryItem> transfers) {
        ComplianceReportVm vm = new ComplianceReportVm();

        Map<String, Long> statusBreakdown = new LinkedHashMap<>();
        statusBreakdown.put("COMPLETED", countByStatus(transfers, "COMPLETED"));
        statusBreakdown.put("PENDING_REVIEW", countByStatus(transfers, "PENDING_REVIEW"));
        statusBreakdown.put("BLOCKED", countByStatus(transfers, "BLOCKED"));
        statusBreakdown.put("REJECTED", countByStatus(transfers, "REJECTED"));
        statusBreakdown.put("FAILED", countByStatus(transfers, "FAILED"));

        Map<String, Long> decisionBreakdown = new LinkedHashMap<>();
        decisionBreakdown.put("PASS", countByDecision(transfers, "PASS"));
        decisionBreakdown.put("FLAG", countByDecision(transfers, "FLAG"));
        decisionBreakdown.put("BLOCK", countByDecision(transfers, "BLOCK"));

        vm.setStatusBreakdown(statusBreakdown);
        vm.setDecisionBreakdown(decisionBreakdown);
        vm.setTotalAuditEvents(0L);
        vm.setTotalAnchoredProofs(0L);
        vm.setTopPolicyHits(List.of());

        return vm;
    }

    private ActivityItemVm toActivityItem(TransferSummaryItem transfer) {
        ActivityItemVm vm = new ActivityItemVm();
        vm.setTransferId(transfer.getId());
        vm.setCreatedAt(resolveActivityTime(transfer));

        switch (safe(transfer.getStatus())) {
            case "COMPLETED" -> {
                vm.setActivityType("TRANSFER_COMPLETED");
                vm.setTitle("Transfer completed");
                vm.setDescription("Transfer " + transfer.getId() + " completed successfully");
                vm.setSeverity("success");
            }
            case "PENDING_REVIEW" -> {
                vm.setActivityType("TRANSFER_PENDING_REVIEW");
                vm.setTitle("Transfer pending review");
                vm.setDescription("Transfer " + transfer.getId() + " requires manual review");
                vm.setSeverity("warning");
            }
            case "BLOCKED" -> {
                vm.setActivityType("TRANSFER_BLOCKED");
                vm.setTitle("Transfer blocked");
                vm.setDescription("Transfer " + transfer.getId() + " was blocked by compliance");
                vm.setSeverity("critical");
            }
            case "REJECTED" -> {
                vm.setActivityType("TRANSFER_REJECTED");
                vm.setTitle("Transfer rejected");
                vm.setDescription("Transfer " + transfer.getId() + " was rejected during review");
                vm.setSeverity("critical");
            }
            case "FAILED" -> {
                vm.setActivityType("TRANSFER_FAILED");
                vm.setTitle("Transfer failed");
                vm.setDescription("Transfer " + transfer.getId() + " failed");
                vm.setSeverity("warning");
            }
            default -> {
                vm.setActivityType("TRANSFER_CREATED");
                vm.setTitle("Transfer created");
                vm.setDescription("Transfer " + transfer.getId() + " was created");
                vm.setSeverity(severityForDecision(transfer.getComplianceDecision()));
            }
        }

        return vm;
    }

    private LocalDateTime resolveActivityTime(TransferSummaryItem transfer) {
        if (transfer.getCompletedAt() != null) {
            return transfer.getCompletedAt();
        }
        if (transfer.getReviewedAt() != null) {
            return transfer.getReviewedAt();
        }
        return transfer.getCreatedAt();
    }

    private String severityForDecision(String decision) {
        return switch (safe(decision)) {
            case "PASS" -> "success";
            case "FLAG" -> "warning";
            case "BLOCK" -> "critical";
            default -> "info";
        };
    }

    private long countByStatus(List<TransferSummaryItem> transfers, String status) {
        return transfers.stream()
                .filter(t -> status.equalsIgnoreCase(safe(t.getStatus())))
                .count();
    }

    private long countByDecision(List<TransferSummaryItem> transfers, String decision) {
        return transfers.stream()
                .filter(t -> decision.equalsIgnoreCase(safe(t.getComplianceDecision())))
                .count();
    }

    private long countByCounterpartyType(List<TransferSummaryItem> transfers, String counterpartyType) {
        return transfers.stream()
                .filter(t -> counterpartyType.equalsIgnoreCase(safe(t.getCounterpartyType())))
                .count();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    @lombok.Getter
    @lombok.Setter
    public static class DashboardSummaryVm {
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

    @lombok.Getter
    @lombok.Setter
    public static class ActivityItemVm {
        private String activityType;
        private String title;
        private String description;
        private Long transferId;
        private String severity;
        private LocalDateTime createdAt;
    }

    @lombok.Getter
    @lombok.Setter
    public static class ComplianceReportVm {
        private Map<String, Long> decisionBreakdown;
        private Map<String, Long> statusBreakdown;
        private long totalAuditEvents;
        private long totalAnchoredProofs;
        private List<Object> topPolicyHits;
    }
}