package com.compliflow.transfer_service.repository;

import com.compliflow.transfer_service.model.AuditEvent;
import com.compliflow.transfer_service.model.ComplianceDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

    List<AuditEvent> findAllByOrderByCreatedAtDesc();

    List<AuditEvent> findByTransferIdOrderByCreatedAtDesc(Long transferId);

    List<AuditEvent> findByTransferIdOrderByCreatedAtAsc(Long transferId);
    List<AuditEvent> findTop50ByOrderByCreatedAtDesc();

    @Query("select count(distinct a.transferId) from AuditEvent a where a.onChainTxHash is not null and a.onChainTxHash <> ''")
    long countDistinctAnchoredTransferIds();
}
