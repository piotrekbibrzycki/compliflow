package com.compliflow.dashboard_web.config;

import com.compliflow.dashboard_web.session.DashboardSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SessionAuthInterceptor implements HandlerInterceptor {

    private final DashboardSessionService dashboardSessionService;

    public SessionAuthInterceptor(DashboardSessionService dashboardSessionService) {
        this.dashboardSessionService = dashboardSessionService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        if (isPublicPath(uri)) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session != null && dashboardSessionService.isAuthenticated(session)) {
            return true;
        }

        response.sendRedirect("/login");
        return false;
    }

    private boolean isPublicPath(String uri) {
        return uri.startsWith("/login")
                || uri.startsWith("/css/")
                || uri.startsWith("/js/")
                || uri.startsWith("/images/")
                || uri.startsWith("/webjars/")
                || uri.equals("/error");
    }
}