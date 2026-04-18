package com.compliflow.transfer_service.dto;

import com.compliflow.transfer_service.model.RestrictedMatchType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestrictedPartyEntryRequestDto {

    @NotNull
    private RestrictedMatchType matchType;

    @NotBlank
    private String matchValue;

    @NotBlank
    private String entityName;

    @NotBlank
    private String source;

    private String sourceReference;
}