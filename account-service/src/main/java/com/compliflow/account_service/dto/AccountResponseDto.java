package com.compliflow.account_service.dto;

import com.compliflow.account_service.model.Account;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponseDto {

    private Long id;
    private String accountNumber;
    private String ownerName;
    private BigDecimal balance;
    private String currency;
    private Integer riskScore;
    private Boolean anonymized;
    private LocalDateTime anonymizedAt;
    private String walletAddress;
    private LocalDateTime createdAt;

    public static AccountResponseDto toDto(Account account) {
        return AccountResponseDto.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .ownerName(account.getOwnerName())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .riskScore(account.getRiskScore())
                .anonymized(account.getAnonymized())
                .anonymizedAt(account.getAnonymizedAt())
                .walletAddress(account.getWalletAddress())
                .createdAt(account.getCreatedAt())
                .build();
    }
}