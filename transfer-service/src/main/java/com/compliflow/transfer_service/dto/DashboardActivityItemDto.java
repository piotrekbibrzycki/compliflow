package com.compliflow.transfer_service.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardActivityItemDto {

    private String activityType;
    private String title;
    private String description;
    private Long transferId;
    private String severity;
    private LocalDateTime createdAt;
}