package com.compliflow.transfer_service.dto;

import com.compliflow.transfer_service.model.ReviewDecision;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferExplanationReviewMetadataDto {

    private boolean reviewed;
    private ReviewDecision reviewDecision;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private String reviewComment;
}