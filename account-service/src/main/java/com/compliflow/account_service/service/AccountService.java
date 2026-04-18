package com.compliflow.account_service.service;

import com.compliflow.account_service.dto.AccountRequestDto;
import com.compliflow.account_service.dto.AccountResponseDto;
import com.compliflow.account_service.dto.AdjustBalanceRequestDto;
import com.compliflow.account_service.exception.AccountNotFoundException;
import com.compliflow.account_service.model.Account;
import com.compliflow.account_service.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public AccountResponseDto getAccountById(Long id) {
        Account account = accountRepository.findById(id).orElseThrow(()->new AccountNotFoundException(id));
        return AccountResponseDto.toDto(account);
    }

    public AccountResponseDto getAccountByNumber(String number) {
        Account account = accountRepository.findByAccountNumber(number).orElseThrow(()->new AccountNotFoundException(number));
        return AccountResponseDto.toDto(account);
    }

    public List<AccountResponseDto> getAccounts() {
        return accountRepository.findAll().stream().map(AccountResponseDto::toDto).toList();
    }

    public AccountResponseDto createAccount(AccountRequestDto accountRequestDto) {

        if (accountRepository.existsByAccountNumber(accountRequestDto.getAccountNumber())) {
            throw new IllegalArgumentException("Account number already exists");
        }

        Account account = Account.builder().accountNumber(accountRequestDto.getAccountNumber()).ownerName(accountRequestDto.getOwnerName())
                .balance(accountRequestDto.getBalance()).currency(accountRequestDto.getCurrency()).build();

        Account saved = accountRepository.save(account);
        log.info("Account created: id = {}, number = {}", saved.getId(), saved.getAccountNumber());
        return AccountResponseDto.toDto(saved);
    }

    @Transactional
    public AccountResponseDto adjustBalance(Long id, AdjustBalanceRequestDto adjustBalanceRequestDto) {
        Account account = accountRepository.findById(id).orElseThrow(()-> new AccountNotFoundException(id));

        BigDecimal newBalance = account.getBalance().add(adjustBalanceRequestDto.getAmount());

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Insufficient balance: current=" + account.getBalance() + ", requested=" + adjustBalanceRequestDto.getAmount());
        }

        account.setBalance(newBalance);
        Account saved = accountRepository.save(account);
        log.info("Balance adjusted: id={}, newBalance={}", id, saved.getBalance());
        return AccountResponseDto.toDto(saved);

    }




}
