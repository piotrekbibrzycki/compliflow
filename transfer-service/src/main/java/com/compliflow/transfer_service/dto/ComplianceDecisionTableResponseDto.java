package com.compliflow.transfer_service.dto;

import com.compliflow.transfer_service.model.ComplianceDecisionTable;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceDecisionTableResponseDto {

    private Long id;
    private String tableCode;
    private String name;
    private String description;
    private String version;
    private Boolean active;
    private LocalDateTime createdAt;
    private List<ComplianceDecisionRuleResponseDto> rules;

    public static ComplianceDecisionTableResponseDto fromEntity(ComplianceDecisionTable table, boolean includeRules) {
        return ComplianceDecisionTableResponseDto.builder()
                .id(table.getId())
                .tableCode(table.getTableCode())
                .name(table.getName())
                .description(table.getDescription())
                .version(table.getVersion())
                .active(table.getActive())
                .createdAt(table.getCreatedAt())
                .rules(includeRules
                        ? table.getRules().stream()
                        .map(ComplianceDecisionRuleResponseDto::fromEntity)
                        .toList()
                        : null)
                .build();
    }
}