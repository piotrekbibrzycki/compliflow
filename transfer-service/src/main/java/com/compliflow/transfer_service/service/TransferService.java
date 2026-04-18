package com.compliflow.transfer_service.service;

import com.compliflow.transfer_service.client.AccountServiceClient;
import com.compliflow.transfer_service.compliance.*;
import com.compliflow.transfer_service.dto.TransferRequestDto;
import com.compliflow.transfer_service.dto.TransferResponseDto;
import com.compliflow.transfer_service.model.*;
import com.compliflow.transfer_service.repository.TransferRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class TransferService {

    private final TransferRepository transferRepository;
    private final AccountServiceClient accountServiceClient;
    private final ComplianceService complianceService;
    private final TransferContextFactory transferContextFactory;
    private final AuditEventService auditEventService;

    public TransferService(TransferRepository transferRepository, AccountServiceClient accountServiceClient, ComplianceService complianceService,
                           TransferContextFactory transferContextFactory,
                           AuditEventService auditEventService) {
        this.transferRepository = transferRepository;
        this.accountServiceClient = accountServiceClient;
        this.complianceService = complianceService;
        this.transferContextFactory = transferContextFactory;
        this.auditEventService = auditEventService;
    }

    @Transactional
    public TransferResponseDto executeTransfer(TransferRequestDto request) {
        if (!accountServiceClient.accountExists(request.getFromAccount())) {
            return failTransfer(request, "Source account not found: " + request.getFromAccount());
        }
        if (!accountServiceClient.accountExists(request.getToAccount())) {
            return failTransfer(request, "Destination account not found: " + request.getToAccount());
        }
        if (request.getFromAccount().equals(request.getToAccount())) {
            return failTransfer(request, "Cannot transfer to the same account");
        }

        TransferContext context = transferContextFactory.build(request);
        ComplianceResult complianceResult = complianceService.evaluate(context);

        Transfer transfer = buildTransferFromRequest(request, complianceResult);
        transfer = transferRepository.save(transfer);

        auditEventService.saveEvaluations(transfer, complianceResult.getEvaluations());

        log.info("Transfer evaluated by compliance: id={}, decision={}, status={}",
                transfer.getId(),
                transfer.getComplianceDecision(),
                transfer.getStatus());

        if (complianceResult.getFinalDecision() == ComplianceDecision.BLOCK) {
            return TransferResponseDto.toDto(transfer);
        }

        if (complianceResult.getFinalDecision() == ComplianceDecision.FLAG) {
            return TransferResponseDto.toDto(transfer);
        }

        Long fromId = accountServiceClient.getAccountIdByNumber(request.getFromAccount());
        Long toId = accountServiceClient.getAccountIdByNumber(request.getToAccount());

        if (fromId == null || toId == null) {
            return failExistingTransfer(transfer, "Failed to resolve account IDs");
        }

        boolean debitSuccess = accountServiceClient.adjustBalance(fromId, request.getAmount().negate());
        if (!debitSuccess) {
            return failExistingTransfer(transfer, "Failed to debit source account (insufficient balance or service unavailable)");
        }

        boolean creditSuccess = accountServiceClient.adjustBalance(toId, request.getAmount());
        if (!creditSuccess) {
            log.warn("Credit failed, compensating debit: transferId={}", transfer.getId());
            boolean compensationSuccess = accountServiceClient.adjustBalance(fromId, request.getAmount());

            if (!compensationSuccess) {
                log.error("CRITICAL: COMPENSATION FAILED transferId={}, manual intervention required", transfer.getId());
            }

            return failExistingTransfer(transfer, "Failed to credit destination account, debit reversed");
        }

        transfer.setStatus(TransferStatus.COMPLETED);
        transfer.setCompletedAt(LocalDateTime.now());
        transfer.setFailureReason(null);
        transfer = transferRepository.save(transfer);

        log.info("Transfer completed: id={}", transfer.getId());

        return TransferResponseDto.toDto(transfer);
    }
    private Transfer buildTransferFromRequest(TransferRequestDto request, ComplianceResult complianceResult) {
        return Transfer.builder()
                .fromAccount(request.getFromAccount())
                .toAccount(request.getToAccount())
                .targetWalletAddress(request.getTargetWalletAddress())
                .amount(request.getAmount())
                .title(request.getTitle())
                .currency(request.getCurrency())
                .paymentRail(request.getPaymentRail() != null ? request.getPaymentRail() : PaymentRail.SEPA)
                .counterpartyType(request.getCounterpartyType() != null ? request.getCounterpartyType() : CounterpartyType.BANK_ACCOUNT)
                .status(resolveInitialStatus(complianceResult.getFinalDecision()))
                .complianceDecision(complianceResult.getFinalDecision())
                .complianceReasonSummary(complianceResult.getSummaryReason())
                .failureReason(complianceResult.getFinalDecision() == ComplianceDecision.BLOCK
                        ? complianceResult.getSummaryReason()
                        : null)
                .build();
    }

    private TransferStatus resolveInitialStatus(ComplianceDecision decision) {
        return switch (decision) {
            case PASS -> TransferStatus.PENDING;
            case FLAG -> TransferStatus.PENDING_REVIEW;
            case BLOCK -> TransferStatus.BLOCKED;
        };
    }

    public TransferResponseDto getTransferById(Long id) {
        Transfer transfer = transferRepository.findById(id).orElseThrow(()-> new IllegalArgumentException("Transfer not found: " + id));
        return TransferResponseDto.toDto(transfer);
    }

    public List<TransferResponseDto> getTransfersByAccount(String accountNumber) {
        return transferRepository.findByFromAccountOrToAccount(accountNumber, accountNumber).stream().map(TransferResponseDto::toDto).toList();
    }

    private TransferResponseDto failTransfer(TransferRequestDto transferRequestDto, String reason) {
        log.warn("Transfer failed before execution: reason={}", reason);
        Transfer transfer = Transfer.builder().fromAccount(transferRequestDto.getFromAccount()).toAccount(transferRequestDto.getToAccount())
                .amount(transferRequestDto.getAmount()).title(transferRequestDto.getTitle())
                .currency(transferRequestDto.getCurrency()).status(TransferStatus.FAILED).failureReason(reason)
                .build();
        transfer = transferRepository.save(transfer);
        return TransferResponseDto.toDto(transfer);
    }

    private TransferResponseDto failExistingTransfer(Transfer transfer, String reason) {
        log.warn("Transfer failed during execution: id={}, reason={}", transfer.getId(), reason);
        transfer.setStatus(TransferStatus.FAILED);
        transfer.setFailureReason(reason);
        transfer = transferRepository.save(transfer);
        return TransferResponseDto.toDto(transfer);

    }
}
