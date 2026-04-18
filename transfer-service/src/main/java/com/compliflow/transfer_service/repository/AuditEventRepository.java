package com.compliflow.transfer_service.repository;

import com.compliflow.transfer_service.model.AuditEvent;
import com.compliflow.transfer_service.model.ComplianceDecision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

    List<AuditEvent> findAllByOrderByCreatedAtDesc();

    List<AuditEvent> findByTransferIdOrderByCreatedAtDesc(Long transferId);

    List<AuditEvent> findByTransferIdOrderByCreatedAtAsc(Long transferId);
}
