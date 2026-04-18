package com.compliflow.dashboard_web.controller;

import com.compliflow.dashboard_web.dto.LoginForm;
import com.compliflow.dashboard_web.service.AccountApiClient;
import com.compliflow.dashboard_web.session.DashboardSessionService;
import com.compliflow.dashboard_web.session.DashboardSessionUser;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthPageController {

    private final AccountApiClient accountApiClient;
    private final DashboardSessionService dashboardSessionService;

    public AuthPageController(AccountApiClient accountApiClient,
                              DashboardSessionService dashboardSessionService) {
        this.accountApiClient = accountApiClient;
        this.dashboardSessionService = dashboardSessionService;
    }

    @GetMapping("/login")
    public String loginPage(Model model, HttpSession session) {
        if (dashboardSessionService.isAuthenticated(session)) {
            return "redirect:/dashboard";
        }

        if (!model.containsAttribute("loginForm")) {
            model.addAttribute("loginForm", new LoginForm());
        }

        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginForm") LoginForm loginForm,
                        BindingResult bindingResult,
                        Model model,
                        HttpSession session) {
        if (bindingResult.hasErrors()) {
            return "auth/login";
        }

        try {
            DashboardSessionUser user = accountApiClient.login(
                    loginForm.getEmail(),
                    loginForm.getPassword()
            );

            dashboardSessionService.storeUser(session, user);
            return "redirect:/dashboard";
        } catch (Exception ex) {
            model.addAttribute("loginError", "Invalid email or password");
            return "auth/login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        dashboardSessionService.clear(session);
        return "redirect:/login";
    }
}