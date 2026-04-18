package com.compliflow.transfer_service.controller;

import com.compliflow.transfer_service.dto.ComplianceDecisionRuleResponseDto;
import com.compliflow.transfer_service.dto.ComplianceDecisionTableResponseDto;
import com.compliflow.transfer_service.service.ComplianceDecisionTableService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compliance")
public class ComplianceDecisionTableController {

    private final ComplianceDecisionTableService complianceDecisionTableService;

    public ComplianceDecisionTableController(ComplianceDecisionTableService complianceDecisionTableService) {
        this.complianceDecisionTableService = complianceDecisionTableService;
    }

    @GetMapping("/decision-tables")
    public List<ComplianceDecisionTableResponseDto> getAllDecisionTables() {
        return complianceDecisionTableService.getAllDecisionTables();
    }

    @GetMapping("/decision-tables/{id}")
    public ComplianceDecisionTableResponseDto getDecisionTableById(@PathVariable Long id) {
        return complianceDecisionTableService.getDecisionTableById(id);
    }

    @GetMapping("/decision-rules")
    public List<ComplianceDecisionRuleResponseDto> getAllDecisionRules() {
        return complianceDecisionTableService.getAllDecisionRules();
    }
}