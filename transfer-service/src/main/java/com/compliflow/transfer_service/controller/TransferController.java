package com.compliflow.transfer_service.controller;

import com.compliflow.transfer_service.dto.TransferRequestDto;
import com.compliflow.transfer_service.dto.TransferResponseDto;
import com.compliflow.transfer_service.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransferResponseDto> getTransferById(@PathVariable Long id) {
        return ResponseEntity.ok(transferService.getTransferById(id));
    }

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<List<TransferResponseDto>> getTransfersByAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(transferService.getTransfersByAccount(accountNumber));
    }

    @PostMapping
    public ResponseEntity<TransferResponseDto> executeTransfer(@Valid @RequestBody TransferRequestDto transferRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transferService.executeTransfer(transferRequestDto));

    }


}
