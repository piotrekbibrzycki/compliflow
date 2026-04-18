package com.compliflow.dashboard_web.session;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class DashboardSessionService {

    public static final String SESSION_USER_KEY = "dashboardUser";

    public void storeUser(HttpSession session, DashboardSessionUser user) {
        session.setAttribute(SESSION_USER_KEY, user);
    }

    public DashboardSessionUser getUser(HttpSession session) {
        Object value = session.getAttribute(SESSION_USER_KEY);
        if (value instanceof DashboardSessionUser user) {
            return user;
        }
        return null;
    }

    public boolean isAuthenticated(HttpSession session) {
        return getUser(session) != null;
    }

    public void clear(HttpSession session) {
        session.invalidate();
    }
}