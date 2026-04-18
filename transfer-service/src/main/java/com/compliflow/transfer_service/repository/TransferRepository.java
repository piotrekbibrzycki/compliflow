package com.compliflow.transfer_service.repository;

import com.compliflow.transfer_service.model.ComplianceDecision;
import com.compliflow.transfer_service.model.CounterpartyType;
import com.compliflow.transfer_service.model.Transfer;
import com.compliflow.transfer_service.model.TransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

    List<Transfer> findByFromAccountOrToAccount(String fromAccount, String toAccount);

    long countByFromAccountAndCreatedAtAfter(String fromAccount, LocalDateTime createdAt);
    List<Transfer> findAllByOrderByCreatedAtDesc();

    List<Transfer> findByStatusOrderByCreatedAtDesc(TransferStatus status);

    long countByStatus(TransferStatus status);

    long countByComplianceDecision(ComplianceDecision complianceDecision);

    long countByCounterpartyType(CounterpartyType counterpartyType);

    List<Transfer> findTop20ByOrderByCreatedAtDesc();

}