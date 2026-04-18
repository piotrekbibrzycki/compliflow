package com.compliflow.transfer_service.repository;

import com.compliflow.transfer_service.model.ComplianceDecisionTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComplianceDecisionTableRepository extends JpaRepository<ComplianceDecisionTable, Long> {

    List<ComplianceDecisionTable> findAllByOrderByIdAsc();

    Optional<ComplianceDecisionTable> findFirstByActiveTrueOrderByIdAsc();
}