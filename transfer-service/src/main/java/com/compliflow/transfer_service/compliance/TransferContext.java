package com.compliflow.transfer_service.compliance;

import com.compliflow.transfer_service.model.CounterpartyType;
import com.compliflow.transfer_service.model.PaymentRail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class TransferContext {

    private final String sourceAccountNumber;
    private final String destinationAccountNumber;
    private final String targetWalletAddress;

    private final BigDecimal amount;
    private final String currency;
    private final String title;

    private final PaymentRail paymentRail;
    private final CounterpartyType counterpartyType;

    private final Integer sourceRiskScore;
    private final Integer destinationRiskScore;

    private final int recentTransfersLastHour;

    private final boolean sourceRestricted;
    private final boolean destinationRestricted;
    private final boolean targetWalletRestricted;
}