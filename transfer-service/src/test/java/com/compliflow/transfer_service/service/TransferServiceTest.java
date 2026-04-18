package com.compliflow.transfer_service.service;

import com.compliflow.transfer_service.client.AccountServiceClient;
import com.compliflow.transfer_service.dto.TransferRequestDto;
import com.compliflow.transfer_service.dto.TransferResponseDto;
import com.compliflow.transfer_service.model.Transfer;
import com.compliflow.transfer_service.model.TransferStatus;
import com.compliflow.transfer_service.repository.TransferRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private AccountServiceClient accountServiceClient;

    @InjectMocks
    private TransferService transferService;

    @Test
    void shouldExecuteTransferSuccessfully() {
        TransferRequestDto request = new TransferRequestDto(
                "ACC-001",
                "ACC-002",
                new BigDecimal("100.00"),
                "Test transfer",
                "PLN"
        );

        Transfer pendingTransfer = Transfer.builder()
                .id(1L)
                .fromAccount("ACC-001")
                .toAccount("ACC-002").amount(new BigDecimal("100.00")).title("Test transfer").currency("PLN").status(TransferStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Transfer completedTransfer = Transfer.builder()
                .id(1L)
                .fromAccount("ACC-001")
                .toAccount("ACC-002").amount(new BigDecimal("100.00")).title("Test transfer").currency("PLN")
                .status(TransferStatus.COMPLETED).createdAt(pendingTransfer.getCreatedAt())
                .completedAt(LocalDateTime.now())
                .build();

        when(accountServiceClient.accountExists("ACC-001")).thenReturn(true);
        when(accountServiceClient.accountExists("ACC-002")).thenReturn(true);
        when(accountServiceClient.getAccountIdByNumber("ACC-001")).thenReturn(1L);
        when(accountServiceClient.getAccountIdByNumber("ACC-002")).thenReturn(2L);
        when(accountServiceClient.adjustBalance(1L, new BigDecimal("-100.00"))).thenReturn(true);
        when(accountServiceClient.adjustBalance(2L, new BigDecimal("100.00"))).thenReturn(true);

        when(transferRepository.save(any(Transfer.class)))
                .thenReturn(pendingTransfer)
                .thenReturn(completedTransfer);

        TransferResponseDto result = transferService.executeTransfer(request);

        assertNotNull(result);
        assertEquals(TransferStatus.COMPLETED, result.getStatus());
        assertEquals("ACC-001", result.getFromAccount());
        assertEquals("ACC-002", result.getToAccount());
        assertEquals(new BigDecimal("100.00"), result.getAmount());
    }

    @Test
    void shouldFailWhenSourceAccountDoesNotExist() {
        TransferRequestDto request = new TransferRequestDto(
                "ACC-001",
                "ACC-002",
                new BigDecimal("100.00"),
                "Test transfer",
                "PLN"
        );

        Transfer failedTransfer = Transfer.builder()
                .id(1L)
                .fromAccount("ACC-001")
                .toAccount("ACC-002").amount(new BigDecimal("100.00"))
                .title("Test transfer")
                .currency("PLN").status(TransferStatus.FAILED)
                .failureReason("Source account not found: ACC-001")
                .createdAt(LocalDateTime.now())
                .build();

        when(accountServiceClient.accountExists("ACC-001")).thenReturn(false);
        when(transferRepository.save(any(Transfer.class))).thenReturn(failedTransfer);

        TransferResponseDto result = transferService.executeTransfer(request);

        assertEquals(TransferStatus.FAILED, result.getStatus());
        assertEquals("Source account not found: ACC-001", result.getFailureReason());
    }

    @Test
    void shouldFailWhenDestinationAccountDoesNotExist() {
        TransferRequestDto request = new TransferRequestDto(
                "ACC-001",
                "ACC-002",
                new BigDecimal("100.00"),
                "Test transfer",
                "PLN"
        );

        Transfer failedTransfer = Transfer.builder()
                .id(1L)
                .fromAccount("ACC-001")
                .toAccount("ACC-002").amount(new BigDecimal("100.00"))
                .title("Test transfer")
                .currency("PLN").status(TransferStatus.FAILED)
                .failureReason("Destination account not found: ACC-002")
                .createdAt(LocalDateTime.now())
                .build();

        when(accountServiceClient.accountExists("ACC-001")).thenReturn(true);
        when(accountServiceClient.accountExists("ACC-002")).thenReturn(false);
        when(transferRepository.save(any(Transfer.class))).thenReturn(failedTransfer);

        TransferResponseDto result = transferService.executeTransfer(request);
        assertEquals(TransferStatus.FAILED, result.getStatus());
        assertEquals("Destination account not found: ACC-002", result.getFailureReason());
    }

    @Test
    void shouldFailWhenTransferIsToSameAccount() {
        TransferRequestDto request = new TransferRequestDto(
                "ACC-001",
                "ACC-001",
                new BigDecimal("100.00"),
                "Test transfer",
                "PLN"
        );

        Transfer failedTransfer = Transfer.builder()
                .id(1L)
                .fromAccount("ACC-001").toAccount("ACC-001")
                .amount(new BigDecimal("100.00")).title("Test transfer")
                .currency("PLN").status(TransferStatus.FAILED).failureReason("Cannot transfer to the same account")
                .createdAt(LocalDateTime.now())
                .build();

        when(accountServiceClient.accountExists("ACC-001")).thenReturn(true);
        when(transferRepository.save(any(Transfer.class))).thenReturn(failedTransfer);
        TransferResponseDto result = transferService.executeTransfer(request);

        assertEquals(TransferStatus.FAILED, result.getStatus());
        assertEquals("Cannot transfer to the same account", result.getFailureReason());
    }

    @Test
    void shouldFailWhenAccountIdsCannotBeResolved() {
        TransferRequestDto request = new TransferRequestDto(
                "ACC-001",
                "ACC-002",
                new BigDecimal("100.00"),
                "Test transfer",
                "PLN"
        );

        Transfer failedTransfer = Transfer.builder()
                .id(1L)
                .fromAccount("ACC-001")
                .toAccount("ACC-002")
                .amount(new BigDecimal("100.00")).title("Test transfer")
                .currency("PLN")
                .status(TransferStatus.FAILED).failureReason("Failed to resolve account IDs")
                .createdAt(LocalDateTime.now())
                .build();

        when(accountServiceClient.accountExists("ACC-001")).thenReturn(true);
        when(accountServiceClient.accountExists("ACC-002")).thenReturn(true);
        when(accountServiceClient.getAccountIdByNumber("ACC-001")).thenReturn(1L);
        when(accountServiceClient.getAccountIdByNumber("ACC-002")).thenReturn(null);
        when(transferRepository.save(any(Transfer.class))).thenReturn(failedTransfer);

        TransferResponseDto result = transferService.executeTransfer(request);

        assertEquals(TransferStatus.FAILED, result.getStatus());
        assertEquals("Failed to resolve account IDs", result.getFailureReason());
    }

    @Test
    void shouldFailWhenDebitFails() {
        TransferRequestDto request = new TransferRequestDto(
                "ACC-001",
                "ACC-002",
                new BigDecimal("100.00"),
                "Test transfer",
                "PLN"
        );

        Transfer pendingTransfer = Transfer.builder()
                .id(1L)
                .fromAccount("ACC-001")
                .toAccount("ACC-002")
                .amount(new BigDecimal("100.00"))
                .title("Test transfer").currency("PLN")
                .status(TransferStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Transfer failedTransfer = Transfer.builder()
                .id(1L)
                .fromAccount("ACC-001").toAccount("ACC-002")
                .amount(new BigDecimal("100.00"))
                .title("Test transfer").currency("PLN")
                .status(TransferStatus.FAILED).failureReason("Failed to debit source account (insufficient balance or service unavailable)")
                .createdAt(pendingTransfer.getCreatedAt())
                .build();

        when(accountServiceClient.accountExists("ACC-001")).thenReturn(true);
        when(accountServiceClient.accountExists("ACC-002")).thenReturn(true);
        when(accountServiceClient.getAccountIdByNumber("ACC-001")).thenReturn(1L);
        when(accountServiceClient.getAccountIdByNumber("ACC-002")).thenReturn(2L);
        when(accountServiceClient.adjustBalance(1L, new BigDecimal("-100.00"))).thenReturn(false);

        when(transferRepository.save(any(Transfer.class)))
                .thenReturn(pendingTransfer)
                .thenReturn(failedTransfer);

        TransferResponseDto result = transferService.executeTransfer(request);

        assertEquals(TransferStatus.FAILED, result.getStatus());
        assertEquals(
                "Failed to debit source account (insufficient balance or service unavailable)",
                result.getFailureReason()
        );
    }

    @Test
    void shouldFailWhenCreditFailsAndCompensateDebit() {
        TransferRequestDto request = new TransferRequestDto(
                "ACC-001",
                "ACC-002",
                new BigDecimal("100.00"),
                "Test transfer",
                "PLN"
        );

        Transfer pendingTransfer = Transfer.builder()
                .id(1L)
                .fromAccount("ACC-001")
                .toAccount("ACC-002")
                .amount(new BigDecimal("100.00"))
                .title("Test transfer").currency("PLN")
                .status(TransferStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Transfer failedTransfer = Transfer.builder()
                .id(1L)
                .fromAccount("ACC-001")
                .toAccount("ACC-002")
                .amount(new BigDecimal("100.00"))
                .title("Test transfer").currency("PLN")
                .status(TransferStatus.FAILED).failureReason("Failed to credit destination account, debit reversed")
                .createdAt(pendingTransfer.getCreatedAt())
                .build();

        when(accountServiceClient.accountExists("ACC-001")).thenReturn(true);
        when(accountServiceClient.accountExists("ACC-002")).thenReturn(true);
        when(accountServiceClient.getAccountIdByNumber("ACC-001")).thenReturn(1L);
        when(accountServiceClient.getAccountIdByNumber("ACC-002")).thenReturn(2L);

        when(accountServiceClient.adjustBalance(1L, new BigDecimal("-100.00"))).thenReturn(true);
        when(accountServiceClient.adjustBalance(2L, new BigDecimal("100.00"))).thenReturn(false);
        when(accountServiceClient.adjustBalance(1L, new BigDecimal("100.00"))).thenReturn(true);

        when(transferRepository.save(any(Transfer.class)))
                .thenReturn(pendingTransfer)
                .thenReturn(failedTransfer);

        TransferResponseDto result = transferService.executeTransfer(request);

        assertEquals(TransferStatus.FAILED, result.getStatus());
        assertEquals("Failed to credit destination account, debit reversed", result.getFailureReason());
    }

    @Test
    void shouldReturnTransferById() {
        Transfer transfer = Transfer.builder()
                .id(1L)
                .fromAccount("ACC-001")
                .toAccount("ACC-002").amount(new BigDecimal("100.00"))
                .title("Test transfer")
                .currency("PLN").status(TransferStatus.COMPLETED)
                .createdAt(LocalDateTime.now()).completedAt(LocalDateTime.now())
                .build();

        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));

        TransferResponseDto result = transferService.getTransferById(1L);

        assertEquals(1L, result.getId());
        assertEquals(TransferStatus.COMPLETED, result.getStatus());
    }

    @Test
    void shouldThrowWhenTransferByIdNotFound() {
        when(transferRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> transferService.getTransferById(1L));
    }

    @Test
    void shouldReturnTransfersByAccount() {
        Transfer transfer = Transfer.builder()
                .id(1L)
                .fromAccount("ACC-001")
                .toAccount("ACC-002").amount(new BigDecimal("100.00"))
                .title("Test transfer")
                .currency("PLN").status(TransferStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();

        when(transferRepository.findByFromAccountOrToAccount("ACC-001", "ACC-001"))
                .thenReturn(List.of(transfer));

        List<TransferResponseDto> result = transferService.getTransfersByAccount("ACC-001");

        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().getId());
    }
}
