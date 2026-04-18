package com.compliflow.transfer_service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferExplanationNarrativeDto {

    private String userExplanation;
    private String adminExplanation;
}