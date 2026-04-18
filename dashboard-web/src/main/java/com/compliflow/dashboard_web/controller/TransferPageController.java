package com.compliflow.dashboard_web.controller;

import com.compliflow.dashboard_web.service.TransferApiClient;
import com.compliflow.dashboard_web.service.TransferApiClient.TransferExplanationResponse;
import com.compliflow.dashboard_web.service.TransferApiClient.TransferSummaryItem;
import com.compliflow.dashboard_web.session.DashboardSessionService;
import com.compliflow.dashboard_web.session.DashboardSessionUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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

        TransferExplanationResponse explanation = transferApiClient.getTransferExplanation(user, id);

        model.addAttribute("pageTitle", "Transfer Details");
        model.addAttribute("pageSubtitle", "Detailed compliance and audit view");
        model.addAttribute("currentUserEmail", user.getEmail());
        model.addAttribute("transfer", transfer);
        model.addAttribute("explanation", explanation);

        return "transfers/details";
    }
}