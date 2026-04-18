package com.compliflow.dashboard_web.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "dashboard.api")
@Getter
@Setter
public class DashboardApiProperties {

    private String accountServiceUrl;
    private String transferServiceUrl;
}