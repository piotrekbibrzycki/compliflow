package com.compliflow.account_service.controller;

import com.compliflow.account_service.dto.AccountRequestDto;
import com.compliflow.account_service.dto.AccountResponseDto;
import com.compliflow.account_service.dto.AdjustBalanceRequestDto;
import com.compliflow.account_service.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponseDto> getAccountById(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<AccountResponseDto> getAccountByNumber(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccountByNumber(accountNumber));
    }

    @GetMapping
    public ResponseEntity<List<AccountResponseDto>> getAccounts() {
        return ResponseEntity.ok(accountService.getAccounts());
    }

    @PostMapping
    public ResponseEntity<AccountResponseDto> createAccount(@Valid @RequestBody AccountRequestDto accountRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(accountRequestDto));
    }

    @PatchMapping("/{id}/balance")
    public ResponseEntity<AccountResponseDto> adjustBalance(@PathVariable Long id, @Valid @RequestBody AdjustBalanceRequestDto adjustBalanceRequestDto) {
        return ResponseEntity.ok(accountService.adjustBalance(id,adjustBalanceRequestDto));
    }

}
