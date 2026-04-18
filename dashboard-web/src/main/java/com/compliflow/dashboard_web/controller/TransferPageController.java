package com.compliflow.dashboard_web.controller;

import com.compliflow.dashboard_web.service.TransferApiClient;
import com.compliflow.dashboard_web.service.TransferApiClient.TransferAuditEventItem;
import com.compliflow.dashboard_web.service.TransferApiClient.TransferProofVerificationItem;
import com.compliflow.dashboard_web.service.TransferApiClient.TransferSummaryItem;
import com.compliflow.dashboard_web.session.DashboardSessionService;
import com.compliflow.dashboard_web.session.DashboardSessionUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class TransferPageController {

    private final DashboardSessionService dashboardSessionService;
    private final TransferApiClient transferApiClient;

    public TransferPageController(DashboardSessionService dashboardSessionService,
                                  TransferApiClient transferApiClient) {
        this.dashboardSessionService = dashboardSessionService;
        this.transferApiClient = transferApiClient;
    }

    @GetMapping("/transfers")
    public String transfers(Model model, HttpSession session) {
        DashboardSessionUser user = dashboardSessionService.getUser(session);

        model.addAttribute("pageTitle", "Transfers");
        model.addAttribute("pageSubtitle", "All transfers and compliance outcomes");
        model.addAttribute("currentUserEmail", user.getEmail());
        model.addAttribute("transfers", transferApiClient.getTransfers(user));

        return "transfers/list";
    }

    @GetMapping("/transfers/{id}")
    public String transferDetails(@PathVariable Long id, Model model, HttpSession session) {
        DashboardSessionUser user = dashboardSessionService.getUser(session);

        List<TransferSummaryItem> transfers = transferApiClient.getTransfers(user);
        TransferSummaryItem transfer = transfers.stream()
                .filter(item -> id.equals(item.getId()))
                .findFirst()
                .orElse(null);

        List<TransferAuditEventItem> auditEvents = transfer == null
                ? List.of()
                : transferApiClient.getTransferAuditEvents(user, id);

        TransferApiClient.TransferProofItem proof = transfer == null
                ? null
                : transferApiClient.getTransferProof(user, id);

        model.addAttribute("pageTitle", "Transfer Details");
        model.addAttribute("pageSubtitle", "Detailed compliance and audit view");
        model.addAttribute("currentUserEmail", user.getEmail());
        model.addAttribute("transfer", transfer);
        model.addAttribute("auditEvents", auditEvents);
        model.addAttribute("proof", proof);

        return "transfers/details";
    }

    @PostMapping("/transfers/{id}/approve")
    public String approveTransfer(@PathVariable Long id,
                                  @RequestParam(name = "comment", required = false) String comment,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        DashboardSessionUser user = dashboardSessionService.getUser(session);

        try {
            transferApiClient.reviewTransfer(user, id, "APPROVE", comment);
            redirectAttributes.addFlashAttribute("successMessage", "Transfer approved successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to approve transfer.");
        }

        return "redirect:/transfers/" + id;
    }

    @PostMapping("/transfers/{id}/reject")
    public String rejectTransfer(@PathVariable Long id,
                                 @RequestParam(name = "comment", required = false) String comment,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        DashboardSessionUser user = dashboardSessionService.getUser(session);

        try {
            transferApiClient.reviewTransfer(user, id, "REJECT", comment);
            redirectAttributes.addFlashAttribute("successMessage", "Transfer rejected successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to reject transfer.");
        }

        return "redirect:/transfers/" + id;
    }

    @PostMapping("/transfers/{id}/proof/anchor")
    public String anchorProof(@PathVariable Long id,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        DashboardSessionUser user = dashboardSessionService.getUser(session);

        try {
            transferApiClient.anchorTransferProof(user, id);
            redirectAttributes.addFlashAttribute("successMessage", "Blockchain proof anchored successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to anchor blockchain proof.");
        }

        return "redirect:/transfers/" + id;
    }

    @PostMapping("/transfers/{id}/proof/verify")
    public String verifyProof(@PathVariable Long id,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        DashboardSessionUser user = dashboardSessionService.getUser(session);

        try {
            TransferProofVerificationItem verification = transferApiClient.verifyTransferProof(user, id);

            boolean verified = verification != null
                    && Boolean.TRUE.equals(verification.getMatchesStoredHash())
                    && Boolean.TRUE.equals(verification.getAnchoredOnChain())
                    && Boolean.TRUE.equals(verification.getContractLookupConfirmed());

            if (verified) {
                redirectAttributes.addFlashAttribute("successMessage", "Blockchain proof verified successfully.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Blockchain proof verification did not confirm integrity.");
            }
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to verify blockchain proof.");
        }

        return "redirect:/transfers/" + id;
    }
}