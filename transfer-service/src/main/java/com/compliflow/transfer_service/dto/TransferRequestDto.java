package com.compliflow.transfer_service.dto;

import com.compliflow.transfer_service.model.CounterpartyType;
import com.compliflow.transfer_service.model.PaymentRail;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequestDto {

    @NotBlank(message = "Source account number is required")
    private String fromAccount;

    @NotBlank(message = "Destination account number is required")
    private String toAccount;

    private String targetWalletAddress;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Transfer title is required")
    private String title;

    @NotBlank(message = "Currency is required")
    private String currency;

    private PaymentRail paymentRail;

    private CounterpartyType counterpartyType;

    /**
     * Backward-compatible constructor for existing tests and old code paths.
     */
    public TransferRequestDto(
            String fromAccount,
            String toAccount,
            BigDecimal amount,
            String title,
            String currency
    ) {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.targetWalletAddress = null;
        this.amount = amount;
        this.title = title;
        this.currency = currency;
        this.paymentRail = PaymentRail.SEPA;
        this.counterpartyType = CounterpartyType.BANK_ACCOUNT;
    }
}