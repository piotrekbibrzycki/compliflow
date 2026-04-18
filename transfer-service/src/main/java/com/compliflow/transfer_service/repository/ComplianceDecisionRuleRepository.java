package com.compliflow.transfer_service.repository;

import com.compliflow.transfer_service.model.ComplianceDecisionRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplianceDecisionRuleRepository extends JpaRepository<ComplianceDecisionRule, Long> {

    List<ComplianceDecisionRule> findAllByOrderByPriorityAscIdAsc();

    List<ComplianceDecisionRule> findByDecisionTableIdOrderByPriorityAscIdAsc(Long decisionTableId);

    List<ComplianceDecisionRule> findByDecisionTableActiveTrueAndActiveTrueOrderByPriorityAscIdAsc();
}