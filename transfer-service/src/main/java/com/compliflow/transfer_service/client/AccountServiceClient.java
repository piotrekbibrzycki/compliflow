package com.compliflow.transfer_service.client;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;
@Slf4j
@Component
public class AccountServiceClient {

    private final RestClient restClient;

    public AccountServiceClient(@Value("${account-service.url}") String accountServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(accountServiceUrl).build();
    }

    public boolean accountExists(String accountNumber) {
        try {
            String authHeader = getAuthorizationHeader();

            restClient.get()
                    .uri("/api/accounts/number/{number}", accountNumber)
                    .headers(headers -> {
                        if (authHeader != null) {
                            headers.set("Authorization", authHeader);
                        }
                        String correlationId = getCorrelationId();
                        if (correlationId != null) {
                            headers.set("X-Correlation-Id", correlationId);
                        }
                    })
                    .retrieve()
                    .toBodilessEntity();

            return true;
        } catch (Exception e) {
            log.warn("Account not found or service unavailable: number = {}", accountNumber);
            return false;
        }
    }

    public AccountDto getAccountByNumber(String accountNumber) {
        try {
            String authHeader = getAuthorizationHeader();

            AccountDto response = restClient.get()
                    .uri("/api/accounts/number/{number}", accountNumber)
                    .headers(headers -> {
                        if (authHeader != null) {
                            headers.set("Authorization", authHeader);
                        }

                        String correlationId = getCorrelationId();
                        if (correlationId != null) {
                            headers.set("X-Correlation-Id", correlationId);
                        }
                    })
                    .retrieve()
                    .body(AccountDto.class);

            if (response != null) {
                return response;
            }

            return null;
        } catch (Exception e) {
            log.warn("Failed to get account by number: number = {}, error = {}", accountNumber, e.getMessage());
            return null;
        }
    }

    public Long getAccountIdByNumber(String accountNumber) {
        AccountDto response = getAccountByNumber(accountNumber);
        return response != null ? response.getId() : null;
    }

    public boolean adjustBalance(Long accountId, BigDecimal amount) {
        try {
            String authHeader = getAuthorizationHeader();

            restClient.patch()
                    .uri("/api/accounts/{id}/balance", accountId)
                    .headers(headers -> {
                        if (authHeader != null) {
                            headers.set("Authorization", authHeader);
                        }

                        String correlationId = getCorrelationId();
                        if (correlationId != null) {
                            headers.set("X-Correlation-Id", correlationId);
                        }
                    })
                    .body(Map.of("amount", amount))
                    .retrieve()
                    .toBodilessEntity();

            log.info("Balance adjusted: accountId={} amount={}", accountId, amount);
            return true;
        } catch (Exception e) {
            log.error("Failed to adjust balance: accountId={}, amount={}, error={}", accountId, amount, e.getMessage());
            return false;
        }
    }

    private String getAuthorizationHeader() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getCredentials() == null) {
            return null;
        }

        return "Bearer " + authentication.getCredentials().toString();
    }

    private String getCorrelationId() {
        return MDC.get("correlationId");
    }
}
