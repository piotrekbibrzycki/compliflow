package com.compliflow.dashboard_web.controller;

import com.compliflow.dashboard_web.service.TransferApiClient;
import com.compliflow.dashboard_web.session.DashboardSessionService;
import com.compliflow.dashboard_web.session.DashboardSessionUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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

        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("pageSubtitle", "Compliance operations overview");
        model.addAttribute("currentUserEmail", user.getEmail());

        model.addAttribute("summary", transferApiClient.getDashboardSummary(user));
        model.addAttribute("activity", transferApiClient.getDashboardActivity(user));
        model.addAttribute("report", transferApiClient.getComplianceSummaryReport(user));

        return "dashboard/index";
    }
}