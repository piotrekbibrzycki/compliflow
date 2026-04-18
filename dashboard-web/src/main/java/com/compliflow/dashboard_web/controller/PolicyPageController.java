package com.compliflow.dashboard_web.controller;

import com.compliflow.dashboard_web.session.DashboardSessionService;
import com.compliflow.dashboard_web.session.DashboardSessionUser;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class PolicyPageController {

    private final DashboardSessionService dashboardSessionService;

    public PolicyPageController(DashboardSessionService dashboardSessionService) {
        this.dashboardSessionService = dashboardSessionService;
    }

    @GetMapping("/policies")
    public String policies(Model model, HttpSession session) {
        DashboardSessionUser user = dashboardSessionService.getUser(session);

        model.addAttribute("pageTitle", "Policies");
        model.addAttribute("pageSubtitle", "Read-only compliance decision rules");
        model.addAttribute("currentUserEmail", user.getEmail());
        model.addAttribute("rules", buildStaticRules());

        return "policies/list";
    }

    private List<PolicyRuleVm> buildStaticRules() {
        return List.of(
                new PolicyRuleVm(
                        "AML-RESTRICTED-SOURCE-ACCOUNT",
                        "1.0",
                        "RESTRICTED_PARTY_MATCH",
                        "BLOCK",
                        1000,
                        false,
                        "Source restricted = true"
                ),
                new PolicyRuleVm(
                        "AML-RESTRICTED-DESTINATION-ACCOUNT",
                        "1.0",
                        "RESTRICTED_PARTY_MATCH",
                        "BLOCK",
                        990,
                        false,
                        "Destination restricted = true"
                ),
                new PolicyRuleVm(
                        "AML-RESTRICTED-TARGET-WALLET",
                        "1.0",
                        "RESTRICTED_WALLET_MATCH",
                        "BLOCK",
                        980,
                        false,
                        "Target wallet restricted = true"
                ),
                new PolicyRuleVm(
                        "AML-LARGE-TRANSFER-THRESHOLD",
                        "1.0",
                        "LARGE_TRANSFER_THRESHOLD",
                        "FLAG",
                        500,
                        true,
                        "Min amount = 10000"
                ),
                new PolicyRuleVm(
                        "AML-HIGH-TRANSFER-FREQUENCY",
                        "1.0",
                        "HIGH_TRANSFER_FREQUENCY",
                        "FLAG",
                        400,
                        true,
                        "Recent transfers in last hour ≥ 3"
                ),
                new PolicyRuleVm(
                        "AML-HIGH-RISK-SOURCE-ACCOUNT",
                        "1.0",
                        "HIGH_RISK_ACCOUNT",
                        "FLAG",
                        300,
                        true,
                        "Source risk score ≥ 80"
                )
        );
    }

    @Getter
    @AllArgsConstructor
    public static class PolicyRuleVm {
        private String policyCode;
        private String policyVersion;
        private String scenarioCode;
        private String decision;
        private Integer priority;
        private Boolean reviewAllowed;
        private String conditions;
    }
}