package com.compliflow.dashboard_web.session;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSessionUser {

    private String email;
    private String token;
}