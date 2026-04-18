package com.compliflow.transfer_service.controller;

import com.compliflow.transfer_service.dto.TransferExplanationResponseDto;
import com.compliflow.transfer_service.dto.TransferRequestDto;
import com.compliflow.transfer_service.dto.TransferResponseDto;
import com.compliflow.transfer_service.dto.TransferReviewRequestDto;
import com.compliflow.transfer_service.model.TransferStatus;
import com.compliflow.transfer_service.service.TransferExplanationService;
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
    private final TransferExplanationService transferExplanationService;

    public TransferController(TransferService transferService, TransferExplanationService transferExplanationService) {
        this.transferService = transferService;
        this.transferExplanationService = transferExplanationService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransferResponseDto> getTransferById(@PathVariable Long id) {
        return ResponseEntity.ok(transferService.getTransferById(id));
    }

    @GetMapping("/{id}/explanation")
    public ResponseEntity<TransferExplanationResponseDto> getTransferExplanation(@PathVariable Long id) {
        return ResponseEntity.ok(transferExplanationService.getTransferExplanation(id));
    }

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<List<TransferResponseDto>> getTransfersByAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(transferService.getTransfersByAccount(accountNumber));
    }

    @PostMapping
    public ResponseEntity<TransferResponseDto> executeTransfer(@Valid @RequestBody TransferRequestDto transferRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transferService.executeTransfer(transferRequestDto));
    }

    @GetMapping
    public List<TransferResponseDto> getAllTransfers() {
        return transferService.getAllTransfers();
    }

    @GetMapping("/status/{status}")
    public List<TransferResponseDto> getTransfersByStatus(@PathVariable TransferStatus status) {
        return transferService.getTransfersByStatus(status);
    }

    @GetMapping("/pending-review")
    public List<TransferResponseDto> getPendingReviewTransfers() {
        return transferService.getPendingReviewTransfers();
    }

    @PatchMapping("/{id}/review")
    public ResponseEntity<TransferResponseDto> reviewTransfer(
            @PathVariable Long id,
            @Valid @RequestBody TransferReviewRequestDto request) {
        return ResponseEntity.ok(transferService.reviewTransfer(id, request));
    }
}