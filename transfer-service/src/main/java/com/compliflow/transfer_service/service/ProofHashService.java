package com.compliflow.transfer_service.service;

import org.springframework.stereotype.Service;
import org.web3j.crypto.Hash;

@Service
public class ProofHashService {

    public String computeIntegrityHash(String canonicalPayloadJson) {
        if (canonicalPayloadJson == null || canonicalPayloadJson.isBlank()) {
            throw new IllegalArgumentException("Canonical payload must not be blank");
        }

        return Hash.sha3String(canonicalPayloadJson);
    }
}