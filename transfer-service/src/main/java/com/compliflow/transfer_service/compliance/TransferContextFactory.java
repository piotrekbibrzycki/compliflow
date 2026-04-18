package com.compliflow.transfer_service.compliance;

import com.compliflow.transfer_service.client.AccountDto;
import com.compliflow.transfer_service.client.AccountServiceClient;
import com.compliflow.transfer_service.dto.TransferRequestDto;
import com.compliflow.transfer_service.model.CounterpartyType;
import com.compliflow.transfer_service.model.PaymentRail;
import com.compliflow.transfer_service.model.RestrictedMatchType;
import com.compliflow.transfer_service.repository.RestrictedPartyEntryRepository;
import com.compliflow.transfer_service.repository.TransferRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TransferContextFactory {

    private final AccountServiceClient accountServiceClient;
    private final TransferRepository transferRepository;
    private final RestrictedPartyEntryRepository restrictedPartyEntryRepository;

    public TransferContextFactory(
            AccountServiceClient accountServiceClient,
            TransferRepository transferRepository,
            RestrictedPartyEntryRepository restrictedPartyEntryRepository
    ) {
        this.accountServiceClient = accountServiceClient;
        this.transferRepository = transferRepository;
        this.restrictedPartyEntryRepository = restrictedPartyEntryRepository;
    }

    public TransferContext build(TransferRequestDto request) {
        AccountDto sourceAccount = accountServiceClient.getAccountByNumber(request.getFromAccount());
        AccountDto destinationAccount = accountServiceClient.getAccountByNumber(request.getToAccount());

        int recentTransfersLastHour = (int) transferRepository.countByFromAccountAndCreatedAtAfter(
                request.getFromAccount(),
                LocalDateTime.now().minusHours(1)
        );

        boolean sourceRestricted = restrictedPartyEntryRepository
                .findFirstByMatchTypeAndMatchValueAndActiveTrue(
                        RestrictedMatchType.ACCOUNT_NUMBER,
                        request.getFromAccount()
                )
                .isPresent();

        boolean destinationRestricted = restrictedPartyEntryRepository
                .findFirstByMatchTypeAndMatchValueAndActiveTrue(
                        RestrictedMatchType.ACCOUNT_NUMBER,
                        request.getToAccount()
                )
                .isPresent();

        String targetWalletAddress = request.getTargetWalletAddress();

        boolean targetWalletRestricted = targetWalletAddress != null
                && !targetWalletAddress.isBlank()
                && restrictedPartyEntryRepository
                .findFirstByMatchTypeAndMatchValueAndActiveTrue(
                        RestrictedMatchType.WALLET_ADDRESS,
                        targetWalletAddress
                )
                .isPresent();

        return TransferContext.builder()
                .sourceAccountNumber(request.getFromAccount())
                .destinationAccountNumber(request.getToAccount())
                .targetWalletAddress(targetWalletAddress)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .title(request.getTitle())
                .paymentRail(request.getPaymentRail() != null ? request.getPaymentRail() : PaymentRail.SEPA)
                .counterpartyType(request.getCounterpartyType() != null ? request.getCounterpartyType() : CounterpartyType.BANK_ACCOUNT)
                .sourceRiskScore(sourceAccount != null && sourceAccount.getRiskScore() != null ? sourceAccount.getRiskScore() : 0)
                .destinationRiskScore(destinationAccount != null && destinationAccount.getRiskScore() != null ? destinationAccount.getRiskScore() : 0)
                .recentTransfersLastHour(recentTransfersLastHour)
                .sourceRestricted(sourceRestricted)
                .destinationRestricted(destinationRestricted)
                .targetWalletRestricted(targetWalletRestricted)
                .build();
    }
}