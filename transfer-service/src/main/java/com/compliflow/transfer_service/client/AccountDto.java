package com.compliflow.transfer_service.client;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class AccountDto {

    private Long id;
    private String accountNumber;
    private String ownerName;
    private BigDecimal balance;
    private String currency;
    private Integer riskScore;
    private Boolean anonymized;
    private String walletAddress;
}