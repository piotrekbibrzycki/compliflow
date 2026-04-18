package com.compliflow.account_service.service;

import com.compliflow.account_service.dto.AccountRequestDto;
import com.compliflow.account_service.dto.AccountResponseDto;
import com.compliflow.account_service.dto.AdjustBalanceRequestDto;
import com.compliflow.account_service.exception.AccountNotFoundException;
import com.compliflow.account_service.model.Account;
import com.compliflow.account_service.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void shouldReturnAccountById() {
        Long accountId = 1L;

        Account account = Account.builder()
                .id(accountId).accountNumber("ACC-001").ownerName("Test User")
                .balance(new BigDecimal("1000.00")).currency("PLN")
                .createdAt(LocalDateTime.now())
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        AccountResponseDto result = accountService.getAccountById(accountId);

        assertEquals(accountId, result.getId());
        assertEquals("ACC-001", result.getAccountNumber());
        assertEquals("Test User", result.getOwnerName());
        assertEquals(new BigDecimal("1000.00"), result.getBalance());
        assertEquals("PLN", result.getCurrency());
    }

    @Test
    void shouldThrowWhenAccountByIdNotFound() {
        Long accountId = 1L;

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.getAccountById(accountId));

    }

    @Test
    void shouldCreateAccountSuccessfully() {
        AccountRequestDto request = new AccountRequestDto(
                "ACC-001",
                "Test User",
                new BigDecimal("1000.00"),
                "PLN"
        );

        Account savedAccount = Account.builder().id(1L).accountNumber("ACC-001").ownerName("Test User")
                .balance(new BigDecimal("1000.00")).currency("PLN").createdAt(LocalDateTime.now()).build();

        when(accountRepository.existsByAccountNumber("ACC-001")).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        AccountResponseDto response = accountService.createAccount(request);

        assertEquals(1L, response.getId());
        assertEquals("ACC-001", response.getAccountNumber());
        assertEquals("Test User", response.getOwnerName());
        assertEquals(new BigDecimal("1000.00"), response.getBalance());
        assertEquals("PLN", response.getCurrency());

    }

    @Test
    void shouldThrowWhenAccountNumberAlreadyExists() {
        AccountRequestDto request = new AccountRequestDto(
                "ACC-001",
                "Test User",
                new BigDecimal("1000.00"),
                "PLN"
        );

        when(accountRepository.existsByAccountNumber("ACC-001")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> accountService.createAccount(request));
    }

    @Test
    void shouldAdjustBalanceSuccessfully() {
        Long accountId = 1L;

        Account account = Account.builder()
                .id(accountId)
                .accountNumber("ACC-001").ownerName("Test User").balance(new BigDecimal("1000.00")).currency("PLN")
                .createdAt(LocalDateTime.now())
                .build();

        AdjustBalanceRequestDto request = new AdjustBalanceRequestDto(new BigDecimal("250.00"));

        Account updatedAccount = Account.builder()
                .id(accountId)
                .accountNumber("ACC-001").ownerName("Test User").balance(new BigDecimal("1250.00")).currency("PLN")
                .createdAt(account.getCreatedAt()).build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(updatedAccount);

        AccountResponseDto result = accountService.adjustBalance(accountId, request);

        assertEquals(new BigDecimal("1250.00"), result.getBalance());
    }

    @Test
    void shouldThrowWhenAdjustedBalanceWouldBeNegative() {
        Long accountId = 1L;

        Account account = Account.builder()
                .id(accountId)
                .accountNumber("ACC-001").ownerName("Test User").balance(new BigDecimal("100.00"))
                .currency("PLN").createdAt(LocalDateTime.now()).build();

        AdjustBalanceRequestDto request = new AdjustBalanceRequestDto(new BigDecimal("-150.00"));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertThrows(IllegalArgumentException.class, () -> accountService.adjustBalance(accountId, request));
    }

    @Test
    void shouldThrowWhenAdjustingBalanceForMissingAccount() {
        Long accountId = 1L;
        AdjustBalanceRequestDto request = new AdjustBalanceRequestDto(new BigDecimal("100.00"));

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.adjustBalance(accountId, request));
    }
}