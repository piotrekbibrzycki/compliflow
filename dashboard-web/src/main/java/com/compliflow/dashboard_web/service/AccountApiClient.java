package com.compliflow.dashboard_web.service;

import com.compliflow.dashboard_web.config.DashboardApiProperties;
import com.compliflow.dashboard_web.session.DashboardSessionUser;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class AccountApiClient {

    private final RestClient restClient;

    public AccountApiClient(DashboardApiProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.getAccountServiceUrl())
                .build();
    }

    public DashboardSessionUser login(String email, String password) {
        AuthResponse response = restClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "email", email,
                        "password", password
                ))
                .retrieve()
                .body(AuthResponse.class);

        if (response == null || response.getToken() == null || response.getToken().isBlank()) {
            throw new IllegalStateException("Login failed: account-service did not return a token");
        }

        return DashboardSessionUser.builder()
                .email(email)
                .token(response.getToken())
                .build();
    }

    @Getter
    @Setter
    public static class AuthResponse {
        private String token;
    }
}