package com.compliflow.transfer_service.dto;

import com.compliflow.transfer_service.model.RestrictedPartyEntry;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RestrictedPartyEntryResponseDto {

    private Long id;
    private String matchType;
    private String matchValue;
    private String entityName;
    private String source;
    private String sourceReference;
    private LocalDateTime lastSyncedAt;
    private Boolean active;
    private LocalDateTime createdAt;

    public static RestrictedPartyEntryResponseDto from(RestrictedPartyEntry entry) {
        return RestrictedPartyEntryResponseDto.builder()
                .id(entry.getId())
                .matchType(entry.getMatchType().name())
                .matchValue(entry.getMatchValue())
                .entityName(entry.getEntityName())
                .source(entry.getSource())
                .sourceReference(entry.getSourceReference())
                .lastSyncedAt(entry.getLastSyncedAt())
                .active(entry.getActive())
                .createdAt(entry.getCreatedAt())
                .build();
    }
}