package com.compliflow.transfer_service.controller;

import com.compliflow.transfer_service.dto.TransferAuditProofResponseDto;
import com.compliflow.transfer_service.dto.TransferAuditProofVerificationResponseDto;
import com.compliflow.transfer_service.service.TransferAuditProofService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
public class TransferAuditProofController {

    private final TransferAuditProofService transferAuditProofService;

    public TransferAuditProofController(TransferAuditProofService transferAuditProofService) {
        this.transferAuditProofService = transferAuditProofService;
    }

    @PostMapping("/{id}/proof/anchor")
    public ResponseEntity<TransferAuditProofResponseDto> anchorTransferProof(@PathVariable Long id) {
        return ResponseEntity.ok(transferAuditProofService.anchorTransferProof(id));
    }

    @GetMapping("/{id}/proof")
    public ResponseEntity<TransferAuditProofResponseDto> getTransferProof(@PathVariable Long id) {
        return ResponseEntity.ok(transferAuditProofService.getTransferProof(id));
    }

    @GetMapping("/{id}/proof/verify")
    public ResponseEntity<TransferAuditProofVerificationResponseDto> verifyTransferProof(@PathVariable Long id) {
        return ResponseEntity.ok(transferAuditProofService.verifyTransferProof(id));
    }
}