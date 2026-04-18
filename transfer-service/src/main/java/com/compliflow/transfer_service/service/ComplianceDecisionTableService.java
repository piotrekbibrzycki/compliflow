package com.compliflow.transfer_service.service;

import com.compliflow.transfer_service.dto.ComplianceDecisionRuleResponseDto;
import com.compliflow.transfer_service.dto.ComplianceDecisionTableResponseDto;
import com.compliflow.transfer_service.model.ComplianceDecisionTable;
import com.compliflow.transfer_service.repository.ComplianceDecisionRuleRepository;
import com.compliflow.transfer_service.repository.ComplianceDecisionTableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ComplianceDecisionTableService {

    private final ComplianceDecisionTableRepository complianceDecisionTableRepository;
    private final ComplianceDecisionRuleRepository complianceDecisionRuleRepository;

    public ComplianceDecisionTableService(
            ComplianceDecisionTableRepository complianceDecisionTableRepository,
            ComplianceDecisionRuleRepository complianceDecisionRuleRepository
    ) {
        this.complianceDecisionTableRepository = complianceDecisionTableRepository;
        this.complianceDecisionRuleRepository = complianceDecisionRuleRepository;
    }

    @Transactional(readOnly = true)
    public List<ComplianceDecisionTableResponseDto> getAllDecisionTables() {
        return complianceDecisionTableRepository.findAllByOrderByIdAsc().stream()
                .map(table -> ComplianceDecisionTableResponseDto.fromEntity(table, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public ComplianceDecisionTableResponseDto getDecisionTableById(Long id) {
        ComplianceDecisionTable table = complianceDecisionTableRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Decision table not found: " + id));

        table.getRules().size();

        return ComplianceDecisionTableResponseDto.fromEntity(table, true);
    }

    @Transactional(readOnly = true)
    public List<ComplianceDecisionRuleResponseDto> getAllDecisionRules() {
        return complianceDecisionRuleRepository.findAllByOrderByPriorityAscIdAsc().stream()
                .map(ComplianceDecisionRuleResponseDto::fromEntity)
                .toList();
    }
}