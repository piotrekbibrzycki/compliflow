package com.compliflow.transfer_service.dto;

import com.compliflow.transfer_service.model.ReviewDecision;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferReviewRequestDto {

    @NotNull
    private ReviewDecision decision;

    private String comment;
}
